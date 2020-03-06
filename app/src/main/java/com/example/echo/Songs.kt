package com.example.echo

import android.os.Parcel
import android.os.Parcelable

class Songs(var songID: Long, var songTitle: String, var artist: String, var songData: String, var dateAdded: Long): Parcelable{

    override fun writeToParcel(p0: Parcel?, p1: Int) {
    }

    override fun describeContents(): Int {
        return 0
    }

    object Statified{
        var nameComparator: Comparator<Songs> = Comparator<Songs>{Songs1, Songs2 ->
            var songOne = Songs1.songTitle.toUpperCase()
            var songTwo = Songs2.songTitle.toUpperCase()
            songOne.compareTo(songTwo)
        }

        var dateComparator: Comparator<Songs> = Comparator<Songs>{Songs1, Songs2 ->
            var songOne = Songs1.dateAdded.toDouble()
            var songTwo = Songs2.dateAdded.toDouble()
            songTwo.compareTo(songOne)
        }
    }




}