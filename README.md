# Song Player

This multi-threaded program plays a song provided within a .txt file. This .txt file must provide a contain the information for each musical note on a separate line. Each note/line must contain two parts: the
frequency and length of the note. The frequency must specify the name of the note (C3 = Middle C), with sharp notes (#) being specified by adding an 'S' to the end of the note name. For example, an A# 
(466.16 Hz) would be specified as A4S. The length of the note is specified with an integer (1 = whole, 2 = half, 4 = quarter, etc.). These two pieces of information must be separated by a space.

At the beginning of the program, a separate thread is created for each note contained within the song file. While the song is playing the main thread will notify each thread when it must play its note and for how
long it should play for. Once the song has finished playing, the program shuts down.

Assuming that the provided song file is formatted correctly, the song can be played by running *ant run -Dsongfile='file_name'*. Three sample song files are provided in this project (Mary had a Little Lamb, Amazing Grace, & Sweet Caroline). If no song file is provided as an argument, Mary had a Little Lamb is played by default by running *ant run*.
