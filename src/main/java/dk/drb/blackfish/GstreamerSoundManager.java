package dk.drb.blackfish;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.peers.media.SoundManager;
import org.gstreamer.Buffer;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.AppSink;

/**
 *
 * @author michael
 */
public class GstreamerSoundManager implements SoundManager {

    private final String location;
    private final Pipeline mainpipe;
    private AppSink sink;
    
    public GstreamerSoundManager(String location) {
        this.location = location;
        Gst.init();
        mainpipe = new Pipeline("mainpipe");
    }
    
    
    public void open() {
        try {
            Element source = ElementFactory.make("souphttpsrc", null);
            source.set("location", location);
            
            final Element hlsdemux = ElementFactory.make("hlsdemux", null);
            final Element queue = ElementFactory.make("queue", null);
            final Element mpegtsdemux = ElementFactory.make("mpegtsdemux", null);
            final Element aacparse = ElementFactory.make("aacparse", null);
            final Element faad = ElementFactory.make("faad", null);
            final Element audioconvert = ElementFactory.make("audioconvert", null);
            final Element audioresample = ElementFactory.make("audioresample", null);
            
            Caps caps = Caps.fromString("audio/x-raw-int,rate=8000,width=16,channels=1");
            Element capsFilter = ElementFactory.make("capsfilter", null);
            capsFilter.set("caps", caps);
            
            sink = (AppSink) ElementFactory.make("appsink", null);
            
            mainpipe.addMany(source, hlsdemux, queue, mpegtsdemux, aacparse, faad, audioconvert, audioresample, capsFilter, sink);
            linkAll(source, hlsdemux);
            linkDelayed(hlsdemux, queue);
            linkAll(queue, mpegtsdemux);
            linkDelayed(mpegtsdemux, aacparse);
            linkAll(aacparse, faad, audioconvert, audioresample, capsFilter, sink);
            mainpipe.play();
        } catch (Exception ex) {
            Logger.getLogger(GstreamerSoundManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        mainpipe.stop();
    }

    public byte[] readData() {
        if(sink == null) {
            return null;
        }
        
        Buffer buffer = sink.pullBuffer();
        ByteBuffer bb = buffer.getByteBuffer();
        byte[] b = new byte[bb.remaining()];
        bb.get(b);
        return b;
    }

    public int writeData(byte[] buffer, int offset, int length) {
        return length;
    }
    
    private static void linkAll(Element ... elements) throws Exception {
        for(int i=0;i<elements.length-1;i++) {
            boolean result = elements[i].link(elements[i+1]);
            if(result == false) {
                throw new Exception("Count not bind element " + i + " to element "+ (i+1));
            }
        }
    }
    
    private static void linkDelayed(final Element e1, final Element e2) {
        e1.connect(new Element.PAD_ADDED() {

            public void padAdded(Element element, Pad pad) {
                pad.link(e2.getStaticPad("sink"));
            }
        });
    }
}
