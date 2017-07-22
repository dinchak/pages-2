import org.monome.pages.api.GroovyAPI
import java.util.ArrayList
import org.monome.pages.api.Command
import org.monome.pages.pages.Page
import org.monome.pages.pages.GroovyPage
import org.monome.pages.pages.QuadrantsPage
import org.monome.pages.configuration.FakeMonomeConfiguration
import org.monome.pages.configuration.MonomeConfiguration
import org.monome.pages.configuration.MonomeConfigurationFactory
import org.monome.pages.configuration.PatternBank

class ArcLivePage extends GroovyAPI {

    String prefix = "/m256-184"

    int pattSens = 40
    def pattLengths = []
    int activePattern = 0
    
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
    boolean drumRackSelected = true

    void init() {
        log("ArcLivePage starting up")
        for (int i = 0; i < 16; i++) {
            loopLength[i] = 192
            pattLengths[i] = 192
            cc1[i] = 0
            cc2[i] = 0
            cc1LastVal[i] = 0
            cc2LastVal[i] = 0
        }
        redrawDevice()
    }

    void stop() {
        log("ArcLivePage shutting down")
    }

    void delta(int enc, int delta) {
        MonomeConfiguration monome = getMyMonome()
        if (enc == 0) {
            pattLengthDelta(monome, delta)
        }
        if (enc == 1) {
            playheadAdjustDelta(monome, delta)
        }
        if (enc == 2) {
            cc1Delta(monome, delta)
        }
        if (enc == 3) {
            cc2Delta(monome, delta)
        }
    }

    void pattLengthDelta(MonomeConfiguration monome, int delta) {
        boolean redraw = false
        if (drumRackSelected) {
            PatternBank patterns = getDrumRackConfig().patternBanks.get(0)
            pattLengths[activePattern] += delta
            int length = patterns.patternLengths[patterns.curPattern]
            if (pattLengths[activePattern] < -pattSens) {
                if (length > 96) {
                    length -= 96
                }
                patterns.patternLengths[patterns.curPattern] = length
                pattLengths[activePattern] = pattSens
                redraw = true
            }
            if (pattLengths[activePattern] > pattSens) {
                if (length < 96 * 4) {
                    length += 96
                }
                patterns.patternLengths[patterns.curPattern] = length
                pattLengths[activePattern] = -pattSens
                redraw = true
            }
        } else {
            loopLengthTurn += delta
            if (loopLengthTurn < -loopLengthSens) {
                if (loopLength[(int) (activeLooper / 2)] > 192) {
                    loopLength[(int) (activeLooper / 2)] -= 192
                    loopLengthTurn = loopLengthSens
                    sendCommandToMIDILoopPage(new Command("length", loopLength[(int) (activeLooper / 2)]))
                    redrawDevice()
                }
            } else if (loopLengthTurn > loopLengthSens) {
                if (loopLength[(int) (activeLooper / 2)] < 192 * 4) {
                    loopLength[(int) (activeLooper / 2)] += 192
                    loopLengthTurn = -loopLengthSens
                    sendCommandToMIDILoopPage(new Command("length", loopLength[(int) (activeLooper / 2)]))
                    redrawDevice()
                }
            }
        }
        
        if (redraw) redrawDevice()
    }
    
    void playheadAdjustDelta(MonomeConfiguration monome, int delta) {
        PatternBank patterns = getDrumRackConfig().patternBanks.get(0)
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
        sendCommandToDrumRackPage(new Command("offsetPattern", args))
        patterns.patternPosition[patternNum] = newPos
    }

    void resetPlayhead() {
        PatternBank patterns = getDrumRackConfig().patternBanks.get(0)
        for (int patternNum = 0; patternNum < patterns.numPatterns; patternNum++) {
            patterns.resetPlayhead(patternNum)
        }
    }
        
    void cc1Delta(MonomeConfiguration monome, int delta) {        
        int newCC = cc1[activeLooper] + delta
        if (newCC > 127) newCC = 127
        if (newCC < 0) newCC = 0
        if (newCC != cc1[activeLooper]) {
            cc1[activeLooper] = newCC
            int chan = (activeLooper / 2) + 12
            ccOut(cc1Num, cc1[activeLooper], chan)
            cc1Draw()
        }
    }
    
    void cc2Delta(MonomeConfiguration monome, int delta) {
        int newCC = cc2[activeLooper] + delta
        if (newCC > 127) newCC = 127
        if (newCC < 0) newCC = 0
        if (newCC != cc2[activeLooper]) {
            cc2[activeLooper] = newCC
            int chan = (activeLooper / 2) + 12
            ccOut(cc2Num, cc2[activeLooper], chan)
            cc2Draw()
        }
    }

    void sendCommandToDrumRackPage(Command command) {
        Page page = getDrumRackConfig().pages.get(0);
        if (page instanceof GroovyPage) {
            ((GroovyPage) page).theApp.sendCommand(command)
        }
    }
    
    FakeMonomeConfiguration getDrumRackConfig() {
        MonomeConfiguration monome = getMyMonome()
        QuadrantsPage quadPage = (QuadrantsPage) monome.pages.get(monome.curPage)
        return quadPage.quadrantConfigurations.get(1).monomeConfigs.get(1)
    }    

    void sendCommandToMIDILoopPage(Command command) {
        Page page = getMIDILoopConfig().pages.get(0);        
        if (page instanceof GroovyPage) {
            ((GroovyPage) page).theApp.sendCommand(command)
        }
    }
    
    FakeMonomeConfiguration getMIDILoopConfig() {
        MonomeConfiguration monome = getMyMonome()
        QuadrantsPage quadPage = (QuadrantsPage) monome.pages.get(monome.curPage)
        return quadPage.quadrantConfigurations.get(1).monomeConfigs.get(2)
    }

    void key(int enc, int key) {
        MonomeConfiguration monome = getMyMonome()
        if (enc == 1) {
            if (key == 0) {
                resetPlayhead()
                sendCommandToDrumRackPage(new Command("resetPlayhead", null))
            }
            if (key == 1) {
                cc1Rec = 1
                cc1RecPos = loopLength[activeLooper]
            }
        }
        if (enc == 2) {
            cmd1Down = key
            if (key == 1) {
                cc1Rec = 1
                cc1RecPos = loopLength[activeLooper]
            }
        }
        if (enc == 3) {
            cmd2Down = key
            if (key == 1) {
                cc2Rec = 1
                cc2RecPos = loopLength[activeLooper]
            }
        }
        redrawDevice()
    }

    void redrawDevice() {
        pattLengthDraw();
        playheadAdjustDraw();
        cc1Draw();
        cc2Draw();
    }
    
    void pattLengthDraw() {
        Integer[] levels = new Integer[64]
        if (drumRackSelected) {
            PatternBank patterns = getDrumRackConfig().patternBanks.get(0)
            int length = patterns.patternLengths[patterns.curPattern]
            int endLed = (int) ((float) length / 96.0f * 16.0f)
            for (int i = 0; i < 64; i++) {
                if (i <= endLed) {
                    levels[i] = 15
                } else {
                    levels[i] = 0
                }
            }
        } else {
            int endLed = (int) ((float) loopLength[(int) (activeLooper / 2)] / 192.0f * 16.0f)
            for (int i = 0; i < 64; i++) {
                if (i <= endLed) {
                    levels[i] = 15
                } else {
                    levels[i] = 0
                }
            }
        }
        map(0, levels)
    }
            
    void playheadAdjustDraw() {
        // get patterns for page 0
        PatternBank patterns = getDrumRackConfig().patternBanks.get(0)
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
    
        
    void cc1Draw() {
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
        map(2, levels)
    }

    void cc2Draw() {
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
        map(3, levels)
    }

    void clock() {
        if (tickNum >= 768) tickNum = 0
        playheadAdjustDraw()
        if (cc1Rec == 0) {
            cc1Draw()
        }
        if (cc2Rec == 0) {
            cc2Draw()
        }
        for (int looper = 0; looper < 16; looper++) {
            if (cc1LastVal[looper] != cc1Val[tickNum % loopLength[looper]][looper]) {
                if (!(cc1Rec == 1 && activeLooper == looper)) {
                    int chan = (looper / 2) + 12
                    ccOut(cc1Num, cc1Val[tickNum % loopLength[looper]][looper], chan)
                    cc1LastVal[looper] = cc1Val[tickNum % loopLength[looper]][looper]
                }
            }
            if (cc2LastVal[looper] != cc2Val[tickNum % loopLength[looper]][looper]) {
                if (!(cc2Rec == 1 && activeLooper == looper)) {
                    int chan = (looper / 2) + 12
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
        tickNum++
    }

    void clockReset() {
        redrawDevice()
    }
    
    void sendCommand(Command cmd) {
        if (cmd.getCmd().equalsIgnoreCase("activeLooper")) {
            drumRackSelected = false
            activeLooper = (Integer) cmd.getParam()
            pattLengthDraw()
        }
        if (cmd.getCmd().equalsIgnoreCase("activePattern")) {
            drumRackSelected = true
            activePattern = (Integer) cmd.getParam()
            MonomeConfiguration monome = getMyMonome()
            pattLengthDraw()
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
