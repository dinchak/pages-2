import org.monome.pages.api.GroovyAPI

class GroovyArcTemplatePage extends GroovyAPI {

    void init() {
        log("GroovyArcTemplatePage starting up")
    }

    void stop() {
        log("GroovyArcTemplatePage shutting down")
    }

    void delta(int enc, int delta) {
        log("delta " + enc + " " + delta)
    }

    void key(int enc, int key) {
        log("key " + enc + " " + key)
    }

    void redrawDevice() {
        clear(0)
        all(0, 15)
    }

    void note(int num, int velo, int chan, int on) {
        noteOut(num, velo, chan, on)
    }

    void cc(int num, int val, int chan) {
        ccOut(num, val, chan)
    }

    void clock() {
        clockOut()
    }

    void clockReset() {
        clockResetOut()
    }
}