package tv.subscriptions.youtube.youtubesubscriptionstv;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class HandleXML {

    private String urlString = null;
    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean parsingComplete = true;
    private ArrayList<String> listVideos = new ArrayList<String>();

    public HandleXML(String url){
        this.urlString = url;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getUrlString() {
        return urlString;
    }

    public ArrayList<String> getListVideos() {
        return listVideos;
    }

    public void parseXMLAndStoreIt(XmlPullParser myParser) {

        int event;
        String text=null;
        boolean inEntry = false;

        try {
            event = myParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                String name=myParser.getName();

                switch (event){
                    case XmlPullParser.START_TAG:

                        if(name.equals("entry")){
                            inEntry = true;
                        }

                        else if(inEntry ==true && name.equals("link")){
                            String hrefVideo=myParser.getAttributeValue(1);
                            String idVideo = hrefVideo.split("=")[1];
                            this.listVideos.add(idVideo);
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;

                    case XmlPullParser.END_TAG:

                        if(name.equals("entry")){
                            inEntry = false;
                        }
                        break;
                }

                event = myParser.next();
            }

            parsingComplete = false;

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchXML(){

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);

                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myparser = xmlFactoryObject.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(stream, null);

                    parseXMLAndStoreIt(myparser);
                    stream.close();
                }
                catch (Exception e) {
                    Log.e("APP", e.toString());
                }
            }
        });
        thread.start();
    }
}
