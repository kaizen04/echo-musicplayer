package com.example.echo.fragments

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.echo.CurrentSongHelper
import com.example.echo.R
import com.example.echo.Songs
import com.example.echo.adapters.FavoriteAdapter
import com.example.echo.databases.EchoDatabase

class FavoriteFragment : Fragment() {

    var myActivity: Activity? = null
    var noFavorites: TextView? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var musicBar: ImageView? = null
    var recyclerView: RecyclerView? = null
    var trackPosition: Int = 0
    var favoriteContent: EchoDatabase? = null
    var refreshList: ArrayList<Songs>? = null
    var getListFromDatabase: ArrayList<Songs>? = null

    object Statified {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        activity?.title = "Favorites"
        favoriteContent = EchoDatabase(myActivity)
        noFavorites = view.findViewById(R.id.noFavorites)
        nowPlayingBottomBar = view.findViewById(R.id.hiddenBarFavScreen)
        songTitle = view?.findViewById(R.id.songTitleFavScreen)
        playPauseButton = view.findViewById(R.id.playPauseButton)
        musicBar = view.findViewById(R.id.defaultMusic)
        recyclerView = view.findViewById(R.id.FavoriteRecycler)
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        display_favorites_by_searching()
        bottomBarSetup()
    }

     override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }

    fun getSongsFromPhone(): ArrayList<Songs>? {
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null)
        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            do {
                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)
                val dateAddedIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))
            }while (songCursor.moveToNext())
        }else{
            return null
        }
        songCursor.close()
        return arrayList
    }

    fun bottomBarSetup(){
        try{
            bottomBarClickHandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            SongPlayingFragment.Statified.mediaplayer?.setOnCompletionListener({
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                SongPlayingFragment.Staticated.onSongComplete()
            })
            if(SongPlayingFragment.Statified.mediaplayer?.isPlaying as Boolean){
                nowPlayingBottomBar?.visibility = View.VISIBLE
            }else {
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler(){
        nowPlayingBottomBar?.setOnClickListener({
            try {
                Statified.mediaPlayer = SongPlayingFragment.Statified.mediaplayer
                val songPlayingFragment = SongPlayingFragment()
                var args = Bundle()
                args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
                args.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
                args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                args.putInt("songId", SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
                args.putInt(
                    "songPosition",
                    SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int
                )
                args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
                args.putString("FavBottomBar", "success")
                songPlayingFragment.arguments = args
                fragmentManager?.beginTransaction()
                    ?.replace(R.id.details_fragment, songPlayingFragment)
                    ?.addToBackStack("FavoriteFragment")
                    ?.commit()
            }catch (e: Exception){
                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        })

        playPauseButton?.setOnClickListener({
            if (SongPlayingFragment.Statified.mediaplayer?.isPlaying as Boolean){
                SongPlayingFragment.Statified.mediaplayer?.pause()
                trackPosition = SongPlayingFragment.Statified.mediaplayer?.currentPosition as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                SongPlayingFragment.Statified.mediaplayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaplayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }

    fun display_favorites_by_searching(){
        if (favoriteContent?.checkSize()as Int > 0){
            noFavorites?.visibility = View.INVISIBLE
            refreshList = ArrayList<Songs>()
            getListFromDatabase = favoriteContent?.queryDBList()
            val fetchListFromDevice = getSongsFromPhone()
            if(fetchListFromDevice != null){
                for (i in 0..fetchListFromDevice.size - 1){
                    for (j in 0..getListFromDatabase?.size as Int - 1){
                        if (getListFromDatabase?.get(j)?.songID == fetchListFromDevice.get(i).songID){
                            refreshList?.add((getListFromDatabase as ArrayList<Songs>)[j])
                        }else{

                        }
                    }
                }
            }
            if (refreshList == null){
                recyclerView?.visibility = View.INVISIBLE
                noFavorites?.visibility = View.VISIBLE
            }else{
                val favoriteAdapter = FavoriteAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                val mLayoutManager = LinearLayoutManager(activity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favoriteAdapter
                recyclerView?.setHasFixedSize(true)
            }
        }else{
            recyclerView?.visibility = View.INVISIBLE
            noFavorites?.visibility = View.VISIBLE
        }
    }
}
