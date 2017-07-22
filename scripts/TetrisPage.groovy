import org.monome.pages.configuration.GroovyAPI

class TetrisPage extends GroovyAPI {
    int[][] state
    int ticks
    int curBlockX
    int curBlockY
    int curBlockShape
    def shapes = [
        [
            [0,0,0,0],
            [0,0,0,0],
            [0,0,0,0],
            [1,1,1,1]
        ],
        [
            [0,0,0,0],
            [0,0,0,0],
            [1,1,0,0],
            [1,1,0,0]
        ],
        [
            [0,0,0,0],
            [0,0,0,0],
            [0,1,1,0],
            [1,1,0,0]
        ],
        [
            [0,0,0,0],
            [0,0,0,0],
            [1,1,0,0],
            [0,1,1,0]
        ],
        [
            [0,0,0,0],
            [0,0,0,0],
            [0,0,1,0],
            [1,1,1,0]
        ],
        [
            [0,0,0,0],
            [0,0,0,0],
            [1,0,0,0],
            [1,1,1,0]
        ],
    ]

    void press(int x, int y, int val) {
        if (val == 1) {
            rotateBlock()
        }
    }
    
    void rotateBlock() {
        
    }
    
    void clock() {
        if (ticks == 0) {
            tick()
        }
        ticks++
        if (ticks == 24) {
            ticks = 0
        }
    }
    
    void tick() {
        moveDown()
        checkForLines()
    }
    
    void moveDown() {
        
    }
    
}
