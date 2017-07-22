import org.monome.pages.api.GroovyAPI
import org.monome.pages.api.Command

class GridStepPage extends GroovyAPI {

    int[][] steps = new int[16][16]
    int velocity = 127
    int tickNum = -1
    int length = 96
    int swing = 0

    void init() {
        log("GridStepPage starting up")
    }

    void stop() {
        log("GridStepPage shutting down")
    }

    void press(int x, int y, int val) {
        if (val == 1) {
            if (steps[x][y] == 0) {
                steps[x][y] = velocity
                led(x, y, 1)
            } else {
                steps[x][y] = 0
                led(x, y, 0)
            }
        }
    }

    void redraw() {
        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY(); y++) {
                if (steps[x][y] > 0) {
                    led(x, y, 1)
                } else {
                    led(x, y, 0)
                }
            }
        }
        int step = ((float) tickNum / (float) length * (float) sizeX())
        col(step, 255, 255)
    }

    void note(int num, int velo, int chan, int on) {
    }

    void cc(int num, int val, int chan) {
    }

    void clock() {
        tickNum++
        if (tickNum == length) {
            tickNum = 0
        }
        int step = ((float) tickNum / (float) length * (float) sizeX())
        if (step % 4 == 3) {
            if (tickNum % ((float) length / (float) sizeX()) != swing) return;
        } else {
            if (tickNum % ((float) length / (float) sizeX()) != 0) return;
        }
        int prevStep = step - 1
        if (prevStep == -1) prevStep = sizeX() - 1
        col(prevStep, 0, 0)
        col(step, 255, 255)
        for (int y = 0; y < sizeY(); y++) {
            int note = y + 24
            if (steps[prevStep][y] > 0) {
                led(prevStep, y, 1)
                noteOut(note, 0, 0, 0)
            }
            if (steps[step][y] > 0) {
                noteOut(note, steps[step][y], 0, 1)
            }
        }
    }

    void clockReset() {
        tickNum = -1
        redraw()
    }
    
    void sendCommand(Command cmd) {
        if (cmd.getCmd().equalsIgnoreCase("swing")) {
            swing = (Integer) cmd.getParam()
        }
        if (cmd.getCmd().equalsIgnoreCase("length")) {
            length = (Integer) cmd.getParam()
            if (tickNum > length) tickNum = tickNum % length
            redraw()
        }
        if (cmd.getCmd().equalsIgnoreCase("velocity")) {
            velocity = (Integer) cmd.getParam()
        }
    }
}