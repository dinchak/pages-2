import org.monome.pages.api.GroovyAPI
import java.util.ArrayList
import org.monome.pages.api.Command
import org.monome.pages.pages.Page
import org.monome.pages.pages.GroovyPage
import org.monome.pages.configuration.MonomeConfiguration
import org.monome.pages.configuration.MonomeConfigurationFactory
import org.monome.pages.configuration.PatternBank

class ArcControlPage extends GroovyAPI {

    String prefix = "/m40h0146"

    int fingersVelo = 127
    int pcSens = 40
    int pageTurn = 0
    int pattSens = 40
    def pattLengths = []
    int activePattern = 0

    int stepVelo = 127
    int swing = 0
    int swingTurn = 0
    int swingSens = 40
    int length = 96
    int lengthTurn = 0
    int lengthSens = 40
    
    int activeLooper = 0
    int loopLengthTurn = 0
    int loopLengthSens = 40
    def loopLength = []
    def cc1 = []
    def cc2 = []
    int[][] cc1Val = new int[768][16]
    int[][] cc2Val = new int[768][16]
    int cc1Rec = 0
    int cc2Rec = 0
    def cc1LastVal = []
    def cc2LastVal = []
    int cc1RecPos = 0
    int cc2RecPos = 0
    int cc1Num = 30
    int cc2Num = 31

    int cmd1Down = 0
    int cmd2Down = 0

    int tickNum = 0

    void init() {
        log("ArcControlPage starting up")
        for (int i = 0; i < 16; i++) {
            loopLength[i] = 192
            pattLengths[i] = 192
            cc1[i] = 0
            cc2[i] = 0
            cc1LastVal[i] = 0
            cc2LastVal[i] = 0
        }
    }

    void stop() {
        log("ArcControlPage shutting down")
    }

    void delta(int enc, int delta) {
        MonomeConfiguration monome = getMyMonome()
        if (enc == 0) {
            if (monome.curPage == 0) {
                livePageEnc0(monome, delta)
            } else if (monome.curPage == 1) {
                gridStepPageEnc0(monome, delta)
            } else if (monome.curPage == 2) {
                midiLoopPageEnc0(monome, delta)
            }
        }
        if (enc == 1) {
            if (monome.curPage == 0) {
                livePageEnc1(monome, delta)
            } else if (monome.curPage == 1) {
                gridStepPageEnc1(monome, delta)
            } else if (monome.curPage == 2) {
                midiLoopPageEnc1(monome, delta)
            }
        }
        if (enc == 2) {
            if (monome.curPage == 0) {
                livePageEnc2(monome, delta)
            } else if (monome.curPage == 1) {
                gridStepPageEnc2(monome, delta)
            } else if (monome.curPage == 2) {
                midiLoopPageEnc2(monome, delta)
            }
        }
        if (enc == 3) {
            handlePageChangeDelta(delta)
        }
    }

    void livePageEnc0(MonomeConfiguration monome, int delta) {
        pattLengths[activePattern] += delta
        PatternBank patterns = monome.patternBanks.get(0)
        int length = patterns.patternLengths[patterns.curPattern]
        if (pattLengths[activePattern] < -pattSens) {
            if (length > 96) {
                length -= 96
            }
            patterns.patternLengths[patterns.curPattern] = length
            pattLengths[activePattern] = pattSens
            redrawDevice()
        }
        if (pattLengths[activePattern] > pattSens) {
            if (length < 96 * 4) {
                length += 96
            }
            patterns.patternLengths[patterns.curPattern] = length
            pattLengths[activePattern] = -pattSens
            redrawDevice()
        }
    }
    
    void livePageEnc1(MonomeConfiguration monome, int delta) {
        PatternBank patterns = monome.patternBanks.get(0)
        if (cmd1Down == 0) {
            movePlayhead(patterns, delta, patterns.curPattern)
        } else {
            for (int patternNum = 0; patternNum < patterns.numPatterns; patternNum++) {
                movePlayhead(patterns, delta, patternNum)
            }
        }
    }
    
    void movePlayhead(PatternBank patterns, int delta, int patternNum) {
        int pos = patterns.patternPosition[patternNum]
        int length = patterns.patternLengths[patternNum]
        int newPos = pos + delta
        if (newPos < 0) {
            newPos = length + delta
        } else if (newPos >= length) {
            newPos -= length
        }
        ArrayList<Integer> args = new ArrayList<Integer>()
        args.add(delta)
        args.add(patternNum)
        sendCommandToPage(new Command("offsetPattern", args))
        patterns.patternPosition[patternNum] = newPos
    }

    void resetPlayhead() {
        MonomeConfiguration monome = getMyMonome()
        PatternBank patterns = monome.patternBanks.get(0)
        for (int patternNum = 0; patternNum < patterns.numPatterns; patternNum++) {
            patterns.resetPlayhead(patternNum)
        }
    }
    
    void livePageEnc2(MonomeConfiguration monome, int delta) {
        int newFingersVelo = fingersVelo  + delta
        if (newFingersVelo > 127) newFingersVelo = 127
        if (newFingersVelo < 0) newFingersVelo = 0
        if (newFingersVelo != fingersVelo) {
            fingersVelo = newFingersVelo
            Command cmd = new Command("velocity", fingersVelo)
            sendCommandToPage(cmd)
            drawLivePageEnc2(monome)
        }
    }

    void gridStepPageEnc0(MonomeConfiguration monome, int delta) {
        lengthTurn += delta
        if (lengthTurn < -lengthSens) {
            if (length > 96) {
                length -= 96
                lengthTurn = lengthSens
                sendCommandToPage(new Command("length", length))
                redrawDevice()
            }
        } else if (lengthTurn > lengthSens) {
            if (length < 96 * 4) {
                length += 96
                lengthTurn = -lengthSens
                sendCommandToPage(new Command("length", length))
                redrawDevice()
            }
        }
    }

    void gridStepPageEnc1(MonomeConfiguration monome, int delta) {
        swingTurn += delta
        if (swingTurn < -swingSens) {
            if (swing > 0) {
                swing--
                sendCommandToPage(new Command("swing", swing))
                swingTurn = swingSens
                redrawDevice()
            }
        } else if (swingTurn > swingSens) {
            if (swing < 8) {
                swing++
                sendCommandToPage(new Command("swing", swing))
                swingTurn = -swingSens
                redrawDevice()
            }
        }
    }

    void gridStepPageEnc2(MonomeConfiguration monome, int delta) {
        int newStepVelo = stepVelo  + delta
        if (newStepVelo > 127) newStepVelo = 127
        if (newStepVelo < 0) newStepVelo = 0
        if (newStepVelo != stepVelo) {
            stepVelo = newStepVelo
            Command cmd = new Command("velocity", stepVelo)
            sendCommandToPage(cmd)
            drawGridStepPageEnc2(monome)
        }
    }

    void midiLoopPageEnc0(MonomeConfiguration monome, int delta) {
        loopLengthTurn += delta
        if (loopLengthTurn < -loopLengthSens) {
            if (loopLength[(int) (activeLooper / 2)] > 192) {
                loopLength[(int) (activeLooper / 2)] -= 192
                loopLengthTurn = loopLengthSens
                sendCommandToPage(new Command("length", loopLength[(int) (activeLooper / 2)]))
                redrawDevice()
            }
        } else if (loopLengthTurn > loopLengthSens) {
            if (loopLength[(int) (activeLooper / 2)] < 192 * 4) {
                loopLength[(int) (activeLooper / 2)] += 192
                loopLengthTurn = -loopLengthSens
                sendCommandToPage(new Command("length", loopLength[(int) (activeLooper / 2)]))
                redrawDevice()
            }
        }
    }
    
    void midiLoopPageEnc1(MonomeConfiguration monome, int delta) {        
        int newCC = cc1[activeLooper] + delta
        if (newCC > 127) newCC = 127
        if (newCC < 0) newCC = 0
        if (newCC != cc1[activeLooper]) {
            cc1[activeLooper] = newCC
            int chan = (activeLooper / 2) + 2
            ccOut(cc1Num, cc1[activeLooper], chan)
            drawMidiLoopPageEnc1(monome)
        }
    }
    
    void midiLoopPageEnc2(MonomeConfiguration monome, int delta) {
        int newCC = cc2[activeLooper] + delta
        if (newCC > 127) newCC = 127
        if (newCC < 0) newCC = 0
        if (newCC != cc2[activeLooper]) {
            cc2[activeLooper] = newCC
            int chan = (activeLooper / 2) + 2
            ccOut(cc2Num, cc2[activeLooper], chan)
            drawMidiLoopPageEnc2(monome)
        }
    }

    void handlePageChangeDelta(int delta) {
        pageTurn += delta
        if (pageTurn < -pcSens) {
            MonomeConfiguration monome = getMyMonome()
            int nextPage = monome.curPage - 1
            if (nextPage < 0) {
                nextPage = monome.numPages - 1
            }
            monome.switchPage(nextPage)
            pageTurn = pcSens
            redrawDevice()
        }
        if (pageTurn > pcSens) {
            MonomeConfiguration monome = getMyMonome()
            int nextPage = monome.curPage + 1
            if (nextPage == monome.numPages) {
                nextPage = 0
            }
            monome.switchPage(nextPage)
            pageTurn = -pcSens
            redrawDevice()
        }
    }
    
    void sendCommandToPage(Command command) {
        MonomeConfiguration monome = getMyMonome()
        Page page = monome.pages.get(monome.curPage)
        if (page instanceof GroovyPage) {
            ((GroovyPage) page).theApp.sendCommand(command)
        }
    }

    void key(int enc, int key) {
        MonomeConfiguration monome = getMyMonome()
        if (enc == 2) {
            cmd2Down = key
        }
        if (enc == 1) {
            if (monome.curPage == 0) {
                if (key == 0) {
                    resetPlayhead()
                    sendCommandToPage(new Command("resetPlayhead", null))
                }
            } 
            if (monome.curPage == 2) {
                if (key == 1) {
                    cc1Rec = 1
                    cc1RecPos = loopLength[activeLooper]
                }
            }
            cmd1Down = key
        }
        if (enc == 2) {
            if (monome.curPage == 2) {
                if (key == 1) {
                    cc2Rec = 1
                    cc2RecPos = loopLength[activeLooper]
                }
            }
        }
        redrawDevice()
    }

    void redrawDevice() {
        MonomeConfiguration monome = getMyMonome()
        if (monome.curPage == 0) {
            drawLivePageEnc0(monome);
            drawLivePageEnc1(monome);
            drawLivePageEnc2(monome);
        }
        if (monome.curPage == 1) {
            drawGridStepPageEnc0(monome);
            drawGridStepPageEnc1(monome);
            drawGridStepPageEnc2(monome);
        }
        if (monome.curPage == 2) {
            drawMidiLoopPageEnc0(monome);
            drawMidiLoopPageEnc1(monome);
            drawMidiLoopPageEnc2(monome);
        }
        drawPageEnc();
    }
    
    void drawLivePageEnc0(MonomeConfiguration monome) {        
        PatternBank patterns = monome.patternBanks.get(0)
        int length = patterns.patternLengths[patterns.curPattern]
        int endLed = (int) ((float) length / 96.0f * 16.0f)
        Integer[] levels = new Integer[64]
        for (int i = 0; i < 64; i++) {
            if (i <= endLed) {
                levels[i] = 15
            } else {
                levels[i] = 0
            }
        }
        map(0, levels)
    }
            
    void drawLivePageEnc1(MonomeConfiguration monome) {
        // get patterns for page 0
        PatternBank patterns = monome.patternBanks.get(0)
        int pos = patterns.patternPosition[patterns.curPattern]
        int length = patterns.patternLengths[patterns.curPattern]
        int endLed = (int) ((float) pos / (float) length * 64.0f)
        Integer[] levels = new Integer[64]
        for (int i = 0; i < 64; i++) {
            if (i <= endLed) {
                levels[i] = 15
            } else {
                levels[i] = 0
            }
        }
        map(1, levels)
    }
    
    void drawLivePageEnc2(MonomeConfiguration monome) {
        int endLed = fingersVelo / 2
        Integer[] levels = new Integer[64]
        for (int i = 0; i < 64; i++) {
            if (i <= endLed) {
                levels[i] = 15
            } else {
                levels[i] = 0
            }
        }
        map(2, levels)
    }
    
    void drawGridStepPageEnc0(MonomeConfiguration monome) {
        int endLed = (int) ((float) length / 96.0f * 16.0f)
        Integer[] levels = new Integer[64]
        for (int i = 0; i < 64; i++) {
            if (i <= endLed) {
                levels[i] = 15
            } else {
                levels[i] = 0
            }
        }
        map(0, levels)
    }

    void drawGridStepPageEnc1(MonomeConfiguration monome) {
        int endLed = (int) ((float) swing * 8.0f)
        Integer[] levels = new Integer[64]
        for (int i = 0; i < 64; i++) {
            if (i <= endLed) {
                levels[i] = 15
            } else {
                levels[i] = 0
            }
        }
        map(1, levels)
    }

    void drawGridStepPageEnc2(MonomeConfiguration monome) {
        int endLed = stepVelo / 2
        Integer[] levels = new Integer[64]
        for (int i = 0; i < 64; i++) {
            if (i <= endLed) {
                levels[i] = 15
            } else {
                levels[i] = 0
            }
        }
        map(2, levels)
    }
    
    void drawMidiLoopPageEnc0(MonomeConfiguration monome) {
        int endLed = (int) ((float) loopLength[(int) (activeLooper / 2)] / 192.0f * 16.0f)
        Integer[] levels = new Integer[64]
        for (int i = 0; i < 64; i++) {
            if (i <= endLed) {
                levels[i] = 15
            } else {
                levels[i] = 0
            }
        }
        map(0, levels)
    }
    
    void drawMidiLoopPageEnc1(MonomeConfiguration monome) {
        int endLed = cc1Val[tickNum % loopLength[activeLooper]][activeLooper] / 2
        if (cc1Rec == 1) {
            endLed = cc1[activeLooper] / 2
        }
        Integer[] levels = new Integer[64]
        for (int i = 0; i < 64; i++) {
            if (i <= endLed) {
                levels[i] = 15
            } else {
                levels[i] = 0
            }
        }
        map(1, levels)
    }

    void drawMidiLoopPageEnc2(MonomeConfiguration monome) {
        int endLed = cc2Val[tickNum % loopLength[activeLooper]][activeLooper] / 2
        if (cc2Rec == 1) {
            endLed = cc2[activeLooper] / 2
        }
        Integer[] levels = new Integer[64]
        for (int i = 0; i < 64; i++) {
            if (i <= endLed) {
                levels[i] = 15
            } else {
                levels[i] = 0
            }
        }
        map(2, levels)
    }

    void drawPageEnc() {
        MonomeConfiguration monome = getMyMonome()
        int numPages = monome.numPages
        int curPage = monome.curPage
        Integer[] levels = new Integer[64]
        for (int i = 0; i < 64; i++) {
            levels[i] = 0
        }
        for (int i = 0; i < numPages; i++) {
            int startLed = (int) ((float) i * 64.0f / (float) numPages)
            levels[startLed] = 15
            if (i == curPage) {
                int endLed = (int) ((float) (i + 1) * 64.0f / (float) numPages)
                for (int j = startLed; j < endLed; j++) {
                    levels[j] = 15
                }
            }
        }
        map(3, levels)
    }

    void clock() {
        if (tickNum >= 768) tickNum = 0
        MonomeConfiguration monome = getMyMonome()
        if (monome.curPage == 0) {
            drawLivePageEnc1(monome)
        }
        for (int looper = 0; looper < 16; looper++) {
            if (cc1LastVal[looper] != cc1Val[tickNum % loopLength[looper]][looper]) {
                if (!(cc1Rec == 1 && activeLooper == looper)) {
                    int chan = (looper / 2) + 2
                    ccOut(cc1Num, cc1Val[tickNum % loopLength[looper]][looper], chan)
                    cc1LastVal[looper] = cc1Val[tickNum % loopLength[looper]][looper]
                }
            }
            if (cc2LastVal[looper] != cc2Val[tickNum % loopLength[looper]][looper]) {
                if (!(cc2Rec == 1 && activeLooper == looper)) {
                    int chan = (looper / 2) + 2
                    ccOut(cc2Num, cc2Val[tickNum % loopLength[looper]][looper], chan)
                    cc2LastVal[looper] = cc2Val[tickNum % loopLength[looper]][looper]
                }
            }
        }
        if (cc1Rec == 1) {
            cc1Val[tickNum % loopLength[activeLooper]][activeLooper] = cc1[activeLooper]
            cc1RecPos--
            if (cc1RecPos == 0) {
                cc1Rec = 0
            }
        }
        if (cc2Rec == 1) {
            cc2Val[tickNum % loopLength[activeLooper]][activeLooper] = cc2[activeLooper]
            cc2RecPos--
            if (cc2RecPos == 0) {
                cc2Rec = 0
            }
        }
        if (monome.curPage == 2) {
            if (cc1Rec == 0) {
                drawMidiLoopPageEnc1(monome)
            }
            if (cc2Rec == 0) {
                drawMidiLoopPageEnc2(monome)
            }
        }
        tickNum++
    }

    void clockReset() {
        redrawDevice()
    }
    
    void sendCommand(Command cmd) {
        if (cmd.getCmd().equalsIgnoreCase("activeLooper")) {
            activeLooper = (Integer) cmd.getParam()
            MonomeConfiguration monome = getMyMonome()
            if (monome.curPage == 2) {
                drawMidiLoopPageEnc0(monome)
                drawMidiLoopPageEnc1(monome)
                drawMidiLoopPageEnc2(monome)
            }
        }
        if (cmd.getCmd().equalsIgnoreCase("activePattern")) {
            activePattern = (Integer) cmd.getParam()
            MonomeConfiguration monome = getMyMonome()
            if (monome.curPage == 0) {
                drawLivePageEnc0(monome)
            }
        }
        if (cmd.getCmd().equalsIgnoreCase("stopCC")) {
            int x = (Integer) cmd.getParam()
            for (int i = 0; i < 768; i++) {
                cc1Val[i][x] = 0
                cc2Val[i][x] = 0
            }
        }
    }

    MonomeConfiguration getMyMonome() {
        MonomeConfiguration monome = MonomeConfigurationFactory.getMonomeConfiguration(prefix)
        return monome
    }

}
