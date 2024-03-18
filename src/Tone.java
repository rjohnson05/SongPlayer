import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Tone {
    private final AudioFormat af;

    Tone(AudioFormat af) {
        this.af = af;
    }

    void playSong(List<BellNote> song) throws LineUnavailableException {
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            System.out.printf("Line: %s", line);
            line.open();
            line.start();

            for (BellNote bn: song) {
                playNote(line, bn);
            }
            line.drain();
        }
    }

    private void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Bell.NoteName.MEASURE_LENGTH_SEC * 1000);
        final int length = Bell.NoteName.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        Bell rest = new Bell("REST");
        line.write(rest.sample(), 0, 50);
    }
}
