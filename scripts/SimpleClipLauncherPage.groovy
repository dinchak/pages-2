import org.monome.pages.api.GroovyAPI;
import org.monome.pages.ableton.AbletonTrack;
import org.monome.pages.ableton.AbletonClip;

class SimpleClipLauncherPage extends GroovyAPI {

    void init() {
        log("SimpleClipLauncherPage initialized")
    }

    void press(int x, int y, int val) {
        if (val == 1) {
            abletonOut().playClip(x, y)
        }
    }

    void redraw() {
        boolean drewLed
        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY(); y++) {
                drewLed = false
                AbletonTrack track = ableton().getTrack(x)
                if (track != null) {
                    AbletonClip clip = track.getClip(y)
                    if (clip != null) {
                        int state = clip.getState()
                        if (state > 0) {
                            led(x, y, 1)
                            drewLed = true
                        }
                    }
                }
                if (!drewLed) {
                    led(x, y, 0)
                }
            }
        }
    }

    void note(int num, int velo, int chan, int on) {
    }

    void cc(int num, int val, int chan) {
    }

    void clock() {
    }

    void clockReset() {
    }

    public void handleAbletonEvent() {
        redraw()
    }
}
