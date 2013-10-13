package dk.drb.blackfish;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import net.sourceforge.peers.FileLogger;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.FileSoundManager;
import net.sourceforge.peers.media.SineSoundManager;
import net.sourceforge.peers.sip.core.useragent.BaseSipListener;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

/**
 *
 * @author michael
 */
public class App {

    public static void main(String[] args) throws SocketException, UnknownHostException, SipUriSyntaxException, InterruptedException {
        SipListener listener = new BaseSipListener() {

            public void error(SipResponse sipResponse) {
                System.out.println("Error: " + sipResponse);
            }
        };
        JavaConfig config = new JavaConfig();
        config.setDomain("klon1.telesal.dk");
        config.setLocalInetAddress(InetAddress.getByName("192.168.87.110"));
        config.setUserPart("0999");
        config.setPassword("VHap6512");

        UserAgent agent = new UserAgent(listener, config, null, new GstreamerSoundManager("http://wtbtshdflash-f.akamaihd.net/i/0202750_Event1@82223/master.m3u8"));
     
        agent.getUac().invite("sip:09991@klon1.telesal.dk", null);
        
        Thread.sleep(200000);
        agent.getUac().unregister();
        agent.close();
    }
}
