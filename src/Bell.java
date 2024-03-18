class Bell implements Runnable {
    enum NoteName {
        // REST Must be the first 'Note'
        REST,
        A4,
        A4S,
        B4,
        C4,
        C4S,
        D4,
        D4S,
        E4,
        F4,
        F4S,
        G4,
        G4S,
        A5,
        A5S,
        B5,
        C5;

        public static final int SAMPLE_RATE = 48 * 1024; // ~48KHz
        public static final int MEASURE_LENGTH_SEC = 2;

        // Circumference of a circle divided by # of samples
        private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;

        private final double FREQUENCY_A_HZ = 440.0d;
        private final double MAX_VOLUME = 127.0d;

        private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

        NoteName() {
            int n = this.ordinal();
            if (n > 0) {
                // Calculate the frequency!
                final double halfStepUpFromA = n - 1;
                final double exp = halfStepUpFromA / 12.0d;
                final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);

                // Create sinusoidal data sample for the desired frequency
                final double sinStep = freq * step_alpha;
                for (int i = 0; i < sinSample.length; i++) {
                    sinSample[i] = (byte)(Math.sin(i * sinStep) * MAX_VOLUME);
                }
            }
        }

        public byte[] sample() {
            return sinSample;
        }
    }

    private NoteName noteName;
    private Thread thread;

    public Bell(String noteName) {
        this.noteName = NoteName.valueOf(noteName);
        this.thread = new Thread(this, noteName);

        thread.start();
    }

    public void run() {}

    public Thread getThread() {
        return this.thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
}

