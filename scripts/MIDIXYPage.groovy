import org.monome.pages.api.GroovyAPI;

class MIDIXYPage extends GroovyAPI {
    
    public int ccX = 30 // x-axis cc number
    public int ccY = 31 // y-axis cc number
    public int curX = 0 // current x value
    public int curY = 0 // current y value
    int delayAmount = 50 // delay in ms between step movements
    int resolution = 4 // xy page resolution (step multiplier)
    AnimateXY thread;

    void init() {
        log("MIDIXYPage starting up")
    }
    
    void stop() {
        log("MIDIXYPage shutting down")
        if (thread != null) {
            thread.stop()
        }
    }

    void press(int x, int y, int val) {
        if (val == 1) {
            if (thread != null) {
                thread.stop()
            }
            thread = new AnimateXY(this, curX, curY, x, y, delayAmount)
            new Thread(thread).start()
        }
    }
    
    void redraw() {
        clear(0)
        led(curX, curY, 1)
    }

    void cc(int num, int val, int chan) {
        ccOut(num, val, chan)
    }
}

class AnimateXY implements Runnable {
    MIDIXYPage page
    int endX
    int endY
    int delayAmount
    public float curX
    public float curY
    float amtX
    float amtY
    float steps
    boolean running
    
    public AnimateXY(MIDIXYPage page, int startX, int startY, int endX, int endY, int delayAmount) {
        this.page = page
        this.endX = endX
        this.endY = endY
        this.delayAmount = delayAmount
        this.curX = (float) startX
        this.curY = (float) startY
        if (Math.abs(startX - endX) > Math.abs(startY - endY)) {
            this.steps = Math.abs(startX - endX) * page.resolution
        } else {
            this.steps = Math.abs(startY - endY) * page.resolution
        }
        amtX = (endX - startX) / this.steps
        amtY = (endY - startY) / this.steps
        this.running = true
    }
    
    public void run() {
        while (running && steps > 0) {
            curX += amtX
            curY += amtY
            int valX = Math.round(curX * (128 / (page.sizeX() - 1)))
            if (valX > 127) {
                valX = 127
            }
            int valY = Math.round(curY * (128 / (page.sizeY() - 1)))
            if (valY > 127) {
                valY = 127
            }
            page.cc(page.ccX, valX, 0)
            page.cc(page.ccY, valY, 0)
            steps--
            page.led(page.curX, page.curY, 0)
            page.curX = Math.round(curX)
            page.curY = Math.round(curY)
            page.led(page.curX, page.curY, 1)
            try {
                Thread.sleep(this.delayAmount)
            } catch (InterruptedException e) {
                e.printStackTrace()
            }
        }
    }
    
    public void stop() {
        running = false
    }
}