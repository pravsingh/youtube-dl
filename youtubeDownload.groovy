import groovy.json.*


def playListId = "PL213D03AC396296D6"

def command = "youtube-dl -j --flat-playlist "+playListId+" > "+playListId+".json"
process = [ 'bash', '-c', command].execute()
result = process.text


File downloadFile = new File('download.sh')

downloadFile.write " "

def vidTitleMap = [:]

def PARREL_DOWNLOADS = 20
def PARREL_CONVERSIONS = 50

File jsonFile = new File( playListId+".json" )

def count = 1

jsonFile.eachLine { line ->
    println "\t\t\t"+line

    if(!line.contains("[Deleted Video]") && !line.contains("[Private Video]")) {
        def videoJson = new JsonSlurper().parseText(line)
        println videoJson.id+" -> "+videoJson.title

        vidTitleMap.put(videoJson.id, videoJson.title)

        downloadFile.append "\n\n\t    echo 'downloading[${count}] ${videoJson.id}' \n"
        downloadFile.append "\t    rm -f *${videoJson.id}*\n"
        downloadFile.append "\t    youtube-dl -f 140 ${videoJson.id} -o temp_${videoJson.id}.m4a &"  + "\n" //audio
        downloadFile.append "\t    youtube-dl -f 134 ${videoJson.id} -o temp_${videoJson.id}.mp4 &"  + "\n" // video

        if(count % PARREL_DOWNLOADS == 0) {
           downloadFile.append "\n\n\t    wait \n"
        }

        count++

    }
}

//now wait for all downloads to finish
downloadFile.append "\n\n\t    wait \n"

count = 0
vidTitleMap.each { vid, title -> 
    if(count % PARREL_CONVERSIONS == 0) {
       downloadFile.append "\t    wait \n"
    }

    //merge audio & video
    downloadFile.append "\t    ffmpeg -i temp_${vid}.mp4 -i temp_${vid}.m4a -c:v copy -c:a aac -strict experimental \'${title}.mp4\' & \n"

    count++
}

//now wait for all conversions to finish
downloadFile.append "\t    wait \n"

downloadFile.append "\t    rm -f temp_*\n"


