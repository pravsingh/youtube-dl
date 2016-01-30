#!/bin/bash

# author: Praveendra Singh
# set of bash functions to help use youtube-dl tool
#pre-requisites: ffmpeg, youtube-dl (https://github.com/rg3/youtube-dl)
#

#compatible with latest version
#download all json of a playlist: youtube-dl -j --flat-playlist 'https://www.youtube.com/playlist?list=PLEpfh9jiEpYTCquWiIScn6VW0auXoO5bf' > all_json.list
# cat all_json.list  | sed -e 's/^.*"id": "\(.*\)",.*/youtube-dl -- \1/g'
#download mp4:  youtube-dl -ci -o "%(title)s.%(ext)s" --restrict-filenames -f mp4 http://www.youtube.com/watch?v=f31WLgRBiRg
#download playlist in mp4: youtube-dl -cit https://www.youtube.com/playlist?list=PLEpfh9jiEpYTCquWiIScn6VW0auXoO5bf
#download a single video: youtube-dl -- CqphyHYTzUM


function ytdlnew()
{
youtube-dl -j --flat-playlist 'https://www.youtube.com/playlist?list=PLEpfh9jiEpYTCquWiIScn6VW0auXoO5bf' > all_json.list
cat all_json.list  | sed -e 's/^.*"id": "\(.*\)",.*/\1/g' | awk '{print "youtube-dl -ci -o \"%(title)s.%(ext)s\" --restrict-filenames -f mp4 http://www.youtube.com/watch?v="$1" &";}' | sh
}


#downloads the metadata for videos from playlist
#input: playlist id (e.g. PL18969FDC598E971F of  http://www.youtube.com/playlist?list=PL18969FDC598E971F)
#output: generates *.info.json files with videoid as file name
function ytlist()
{
        youtube-dl -i --get-filename -o "%(id)s"  --write-info-json http://www.youtube.com/playlist?list=$1
}

#gets video id from *.info.json and triggers the download for each of them
function ytdownload()
{

        ls | grep "info.json" | sed -e 's/\./ /g' | awk '{print "youtube-dl -ci -o \"%(title)s.%(ext)s\" --max-quality --restrict-filenames http://www.youtube.com/watch?v="$1" ";}' | sh

        ytmp3
}

#once above 2 methods complete, run this to convert .mp4 to .mp3
function ytmp3()
{
        #rename files with _ for non-alphanumeric chars
        for i in *.mp4 *.flv; do
          j=`echo $i | sed -e 's/[^a-zA-Z0-9]/_/g' | sed -e 's/_mp4/.mp4/g' | sed -e 's/_flv/.flv/g' | sed -e 's/\`/_/g'`
          echo "mv \"$i\"  $j" | sh
        done;

          echo "set -x" > convert.sh
          ls | grep -e '.*\.mp4$' | awk '{print "ffmpeg -n -i "$1" "$1".mp3 ";}' >> convert.sh
          ls | grep -e '.*\.flv$' | awk '{print "ffmpeg -n -i "$1" "$1".mp3 ";}' >> convert.sh

          chmod 755 convert.sh
          echo "convert.sh written. execute this to convert to mp3"
}
