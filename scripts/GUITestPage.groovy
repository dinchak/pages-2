import org.monome.pages.api.GroovyAPI
import java.awt.FlowLayout

class GUITestPage extends GroovyAPI {

    void init() {
        def builder = new groovy.swing.SwingBuilder()
        def gui = builder.frame(
            size: [290, 100],
            title: 'sup'
        ) {
            panel(layout: new FlowLayout()) {
                panel(layout: new FlowLayout()) {
                    checkBox(text: 'sup')
                }
                button(text: 'Button', actionPerformed: {
                    builder.optionPane(message: 'test').createDialog(null, 'Blah').show()
                })
            }
        }
        gui.show()
    }

    void stop() {
        log("GUITestPage shutting down")
    }

    void press(int x, int y, int val) {
        led(x, y, val)
    }

    void redrawDevice() {
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
}