import org.monome.pages.api.GroovyAPI;

class MIDIBomberPage extends GroovyAPI {

    def scale = [2,1,2,2,1,2,2] // minor scale
    def key = "C"
    int speed = 24 // in 1/96th notes
    int frequency = 4 // 1 in (frequency * sizeY) chance of generating a note
    
    int[][] world
    int ticks
    def notes = []
    Random rand
    def explosions = []
    def keys = ["C": 24, "C#": 25, "D": 26, "D#": 27, "E": 28, "F": 29, "F#": 30, "G": 31, "G#": 32, "A": 33, 'A#': 34, "B": 35]
    
    void init() {
        log("MIDIBomberPage starting up")
        world = new int[sizeX()][sizeY()]
        ticks = 0
        rand = new Random()
        clear(0)
    }

    void press(int x, int y, int val) {
        for (explosion in explosions) {
            if (explosion.x == x && explosion.y == y) {
                return
            }
        }
        explosions.add([x: x, y: y, counter: 0])
    }
   
    void redrawWorld() {
        for (int x = 0; x < sizeX(); x++) {
            def cols = []
            for (int quad = 0; quad < sizeY(); quad += 8) {
                int val = 0
                for (int y = 0; y < quad + 8; y++) {
                    val += (2 ** (y - quad)) * world[x][y]
                }
                cols.add(val)
            }
            col(x, cols)
        }
    }

    void redrawExplosions() {
        for (int i = 0; i < explosions.size(); i++) {
            def explosion = explosions[i] 
            if (explosion.counter == 0) {
                playNote(explosion.x, explosion.y)
            }
            if (explosion.counter == 1) {
                playNote(explosion.x, explosion.y)
                playNote(explosion.x, explosion.y-1)
                playNote(explosion.x, explosion.y+1)
                playNote(explosion.x-1, explosion.y)
                playNote(explosion.x+1, explosion.y)
            }
            if (explosion.counter == 2) {
                unplayNote(explosion.x, explosion.y)
                unplayNote(explosion.x, explosion.y-1)
                unplayNote(explosion.x, explosion.y+1)
                unplayNote(explosion.x-1, explosion.y)
                unplayNote(explosion.x+1, explosion.y)
                playNote(explosion.x, explosion.y)
                playNote(explosion.x-1, explosion.y-1)
                playNote(explosion.x+1, explosion.y+1)
                playNote(explosion.x-1, explosion.y+1)
                playNote(explosion.x+1, explosion.y-1)
            }
            if (explosion.counter == 3) {
                unplayNote(explosion.x, explosion.y)
                unplayNote(explosion.x-1, explosion.y-1)
                unplayNote(explosion.x+1, explosion.y+1)
                unplayNote(explosion.x-1, explosion.y+1)
                unplayNote(explosion.x+1, explosion.y-1)
                explosions -= explosion
            } else {
                explosions[i].counter++
            }
        }
    }
    
    void playNote(int x, int y) {
        if (x >= sizeX() || x < 0 || y >= sizeY() || y < 0) return
        led(x, y, 1)
        if (world[x][y] == 1) {
            world[x][y] = 0
            int velocity = rand.nextInt(128)
            int baseKey = keys[key]
            int note = baseKey + (12 * x)
            for (int i = 0; i < y; i++) {
                if (i > 0) {
                    note += scale[i-1]
                }
            }
            noteOut(note, velocity, 0, 1)
            notes.add(note)
        }
    }
    
    void unplayNote(int x, int y) {
        if (x >= sizeX() || x < 0 || y >= sizeY() || y < 0) return
        led(x, y, world[x][y])
    }

    void clock() {
        if (ticks % speed == 0) {
            tickWorld()
        }
        if (ticks % (speed / 4) == 0) {
            redrawExplosions()
        }
        ticks++
    }

    void tickWorld() {
        if (world == null) return        
        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY(); y++) {
                if (x > 0) {
                    world[x - 1][y] = world[x][y]
                }
            }
            if (x == 7) {
                for (int y = 0; y < sizeY(); y++) {
                    if (rand.nextInt(sizeY()) < 1 * frequency) {
                        world[x][y] = 1
                    } else {
                        world[x][y] = 0
                    }
                }
            }
        }
        for (int i = 0; i < notes.size(); i++) {
            def note = notes[i]
            noteOut(note, 0, 0, 0)
            notes[i] -= note
        }
        redrawWorld()
    }

    void clockReset() {
        init()
    }
}