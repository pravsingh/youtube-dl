#!/bin/bash

# author: Praveendra Singh
# set of bash functions to help use youtube-dl tool
#pre-requisites: ffmpeg, youtube-dl (https://github.com/rg3/youtube-dl)
#

#downloads the metadata for videos from playlist
#input: playlist id (e.g. PL18969FDC598E971F of  http://www.youtube.com/playlist?list=PL18969FDC598E971F)
#output: generates *.info.json files with videoid as file name
function ytlist()
{
        mkdir $1
        cd $1
        youtube-dl -i --get-filename -o "%(id)s"  --write-info-json http://www.youtube.com/playlist?list=$1
}

#gets video id from *.info.json and triggers the download for each of them
function ytdownload()
{

        ls | grep "info.json" | sed -e 's/\./ /g' | awk '{print "youtube-dl -ci -o \"%(title)s.%(ext)s\" --max-quality --restrict-filenames http://www.youtube.com/watch?v="$1" ";}' | sh

}

#once above 2 methods complete, run this to convert .mp4 to .mp3
function ytmp3()
{
        #rename files with _ for non-alphanumeric chars
        for i in *.mp4 *.flv; do
          j=`echo $i | sed -e 's/[^a-zA-Z0-9]/_/g' | sed -e 's/_mp4/.mp4/g' | sed -e 's/_flv/.flv/g' | sed -e 's/\`/_/g'`
          echo "mv \"$i\"  $j" | sh
        done;

          ls | grep -e '.*\.mp4$' | awk '{print "ffmpeg -i "$1" "$1".mp3 &";}' | sh
          ls | grep -e '.*\.flv$' | awk '{print "ffmpeg -i "$1" "$1".mp3 &";}' | sh
}
