import groovy.json.*


def playListId = "PL213D03AC396296D6"

def command = "youtube-dl -j --flat-playlist "+playListId+" > "+playListId+".json"
process = [ 'bash', '-c', command].execute()
result = process.text


File downloadFile = new File('download.sh')

downloadFile.write " "


File jsonFile = new File( playListId+".json" )

jsonFile.eachLine { line ->

    println "\t\t\t"+line

    if(!line.contains("[Deleted Video]") && !line.contains("[Private Video]")) {
        def videoJson = new JsonSlurper().parseText(line)
        

        println videoJson.id+" -> "+videoJson.title

        downloadFile.append "\t   echo 'downloading "+videoJson.id+ "' \n\n"
        downloadFile.append "\t    rm -f *"+videoJson.id+"*" + "\n"
        downloadFile.append "\t    youtube-dl -f 140 "+videoJson.id+" -o "+videoJson.id+".m4a &"  + "\n" //audio
        downloadFile.append "\t    youtube-dl -f 134 "+videoJson.id+" -o "+videoJson.id+".mp4 &"  + "\n" // video
        downloadFile.append "\t    wait" + "\n"
        //merge audio & video
        downloadFile.append "\t    ffmpeg -i "+videoJson.id+".mp4 -i "+videoJson.id+".m4a -c:v copy -c:a aac -strict experimental \'"+videoJson.title+".mp4\'" + "\n"
        downloadFile.append "\t    rm -f *"+videoJson.id+"*" + "\n"

    }

}


