import groovy.json.*


def playListId = "PL213D03AC396296D6"

def command = "youtube-dl -j --flat-playlist "+playListId+" > "+playListId+".json"
process = [ 'bash', '-c', command].execute()
result = process.text


File downloadFile = new File('download.sh')

downloadFile.write " "

def vidTitleMap = [:]

def PARREL_DOWNLOADS = 10
def PARREL_CONVERSIONS = 20

File jsonFile = new File( playListId+".json" )

def count = 1

jsonFile.eachLine { line ->
    println "\t\t\t"+line

    if(!line.contains("[Deleted Video]") && !line.contains("[Private Video]")) {
        def videoJson = new JsonSlurper().parseText(line)
        println videoJson.id+" -> "+videoJson.title

        vidTitleMap.put(videoJson.id, videoJson.title)

        def audio = "temp_${videoJson.id}.audio"
        def video = "temp_${videoJson.id}.video"

    	File audioFile = new File(audio)
    	File videoFile = new File(video)

        
    	//download if not already downloaded	
    	if(!audioFile.exists()) {
    		downloadFile.append "\t    rm -f ${audio}\n"
            downloadFile.append "\t    youtube-dl -f 140 \'https://www.youtube.com/watch?v=${videoJson.id}\' -o ${audio} &"  + "\n" //audio
            count++
        }

        if( !videoFile.exists()) {
            downloadFile.append "\t    rm -f ${video}\n"
    		downloadFile.append "\t    youtube-dl -f 134 \'https://www.youtube.com/watch?v=${videoJson.id}\' -o ${video} &"  + "\n" // video
            count++
        }
    	
        if(count % PARREL_DOWNLOADS == 0) {
    	   downloadFile.append "\n\n\t    wait \n"
    	}

    	
    }
}

//now wait for all downloads to finish
downloadFile.append "\n\n\t    wait \n"

count = 0
vidTitleMap.each { vid, title -> 

    title = title.replaceAll("[^A-Za-z0-9 ]", "")

    def fileName = "${title}.mp4"

    downloadFile.append "\t    rm -f \'"+fileName+"\'\n"
    
     if(count % PARREL_CONVERSIONS == 0) {
        downloadFile.append "\t    wait \n"
     }

    //merge audio & video
    downloadFile.append "\t    ffmpeg -i temp_${vid}.video -i temp_${vid}.audio -c:v copy -c:a aac -strict experimental \'${fileName}\'  \n"

    count++
}

//now wait for all conversions to finish
downloadFile.append "\t    wait \n"

downloadFile.append "\t    rm -f temp_*\n"


