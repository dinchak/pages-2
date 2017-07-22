import org.monome.pages.api.GroovyAPI

class ArcCC extends GroovyAPI {

    def ccNum = [30, 31, 32, 33]
    int midiChannel = 0
    def loopLength = 8 * 24

    int[] cc = new int[4]
    int[][] ccVal = new int[4][768]
    int[][] ccLastVal = new int[4]
    int[] ccRec = new int[4]
    int[] ccRecPos = new int[4]
    int[] ccPlaying = new int[4]

    int tickNum = 0

    void init() {
        log("ArcCC starting up")
        redrawDevice()
    }

    void stop() {
        log("ArcCC shutting down")
    }

    void delta(int enc, int delta) {
        int newCC = cc[enc] + delta
        if (newCC > 127) newCC = 127
        if (newCC < 0) newCC = 0
        if (newCC != cc[enc]) {
            cc[enc] = newCC
            ccOut(ccNum[enc], cc[enc], midiChannel)
            ccDraw(enc)
        }
    }

    void key(int enc, int key) {
        if (key == 1) {
            if (ccPlaying[enc] == 0) {
                ccRec[enc] = 1
                ccRecPos[enc] = loopLength
                ccPlaying[enc] = 1
            } else {
                ccRec[enc] = 0
                ccPlaying[enc] = 0
            }
        }
        ccDraw(enc)
    }

    void redrawDevice() {
        for (int enc = 0; enc < 4; enc++) {
            ccDraw(enc)
        }
    }

    void ccDraw(enc) {
        int endLed
        if (ccPlaying[enc]) {
            endLed = ccVal[enc][tickNum % loopLength] / 2
        } else {
            endLed = cc[enc] / 2           
        }
        if (ccRec[enc] == 1) {
            endLed = cc[enc] / 2
        }
        Integer[] levels = new Integer[64]
        for (int i = 0; i < 64; i++) {
            if (i <= endLed) {
                levels[i] = 15
            } else {
                levels[i] = 0
            }
        }
        map(enc, levels)        
    }
    
    void clock() {
        if (tickNum >= 768) tickNum = 0
        for (int enc = 0; enc < 4; enc++) {
            if (ccPlaying[enc] == 1) {
                if (ccRec[enc] == 0) {
                    if (ccLastVal[enc] != ccVal[enc][tickNum % loopLength]) {
                        ccOut(ccNum[enc], ccVal[enc][tickNum % loopLength], midiChannel)
                        ccLastVal[enc] = ccVal[enc][tickNum % loopLength]
                        ccDraw(enc);
                    }
                } else {
                    ccOut(ccNum[enc], cc[enc], midiChannel);
                    ccDraw(enc);
                }
                if (ccRec[enc] == 1) {
                    ccVal[enc][tickNum % loopLength] = cc[enc]
                }
                ccRecPos[enc]--
                if (ccRecPos[enc] == 0) {
                    ccRec[enc] = 0
                }
            }
        }
        tickNum++
    }

    void clockReset() {
        tickNum = 0
        redrawDevice()
    }

    void cc(int num, int val, int chan) {
        if (chan != midiChannel) {
            return;
        }
        for (int enc = 0; enc < 4; enc++) {
            if (ccNum[enc] == num) {
                cc[enc] = val;
                ccDraw(enc);
            }
        }
    }
}
