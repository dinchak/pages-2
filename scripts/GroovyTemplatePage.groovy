import org.monome.pages.api.GroovyAPI

class GroovyTemplatePage extends GroovyAPI {

    void init() {
        log("GroovyTemplatePage starting up")
    }

    void stop() {
        log("GroovyTemplatePage shutting down")
    }

    void press(int x, int y, int val) {
        led(x, y, val)
    }

    void redraw() {
        clear(0)
        led(0, 0, 1)
        row(1, 255, 255)
        col(2, 255, 255)
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
    
    void tilt(int n, int x, int y, int z) {
    }
}