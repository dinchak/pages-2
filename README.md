pages-2
=======

Web based documentation is available here: http://pages.post-digital.net
The old Google Code repository with older releases and information can be found here: http://code.google.com/p/monome-pages

Pages is a monome application that lets you control multiple different applications at the same time.  It does this by introducing a control layer on top of existing applications that allows you to switch between different 'pages', each with its own application running on it.

Summary of Page Types
=====================

* Ableton Clip Launcher Page
This page turns the monome into an interface for launching clips and controlling the state of tracks in Ableton Live.  Many controls are available including tempo up/down, overdub, undo, record enabling of tracks, mute/solo, and others.

* Ableton Live Looper Page
Same as the Ableton Clip Launcher Page but sends clip trigger events after a certain amount of bars to automatically slice loops after they're recorded.  contains a set of controls in the bottom right that adjust the loop length.

* Ableton Scene Launcher Page
Same as the Ableton Clip Launcher Page except that the far left column will flash the selected scene and allow you to trigger entire scenes.  The bottom buttons on the left column are previous/next scene.

* External Application Page
Allow you to route another application through pages and use it on it's own page, for example, MLR.

* Groovy Page
Write your own applications! See http://pages.post-digital.net/docs/groovy_page

* Machine Drum Interface Page
Provides controls for manipulating and randomizing drum kits in an Elektron MachineDrum.

* MIDI Keyboard Page
Turns the monome into a virtual MIDI keyboard.

* MIDI Faders Page
Turns the monome into a set of virtual faders.

* MIDI Sequencer Page
Turns the monome into a 64-step MIDI sequencer.

* MIDI Sequencer Page Poly
An expanded MIDI Sequencer Page with the ability to run multiple patterns at the same time.

* MIDI Triggers Page
Turns the monome into virtual MIDI buttons with toggle mode or trigger mode per row/column.

And a few more, plus the included Groovy Scripts.

Setup Virtual MIDI Devices
==========================

You will need to install/enable a set of virtual MIDI devices if you wish to route pages MIDI applications to an audio application (ie. Ableton Live). Most users will want to do this.

*** Windows ***

MIDI Yoke or loopbe should both work fine for Windows users. Follow the instructions on the web sites to install. Midiyoke is 32 bit only and will not communicate with 64 bit java running pages on Windows 7. Use 32-bit java for pages with midiyoke on Windows 7.

OS X Virtual MIDI Devices Setup

*** OSX ***

The IAC driver comes standard with OS X and will work fine for OS X users. A tutorial on enabling this driver can be found here: http://fox-gieg.com/tutorials/2007/inter-application-midi/


LiveOSC Setup
=============

The LiveOSC folder contains two subfolders: LiveOSC-Windows and LiveOSC-OSX.  Choose the appropriate one for your OS and go inside the folder.

If on Windows:
1. Copy the LiveOSC-Windows/LiveOSC folder to your Ableton folder under the MIDI Remote Scripts folder.  Your final directory path should be like this: MIDI Remote Scripts/LiveOSC/LiveOSC.py

If on OSX:
1. Open Finder and go to Applications.
2. Control-click on Ableton and pick 'Show Package Contents'.
3. Go to App-Resources/MIDI Remote Scripts.
4. Open a second finder window and copy the LiveOSC folder inside LiveOSC-OSX to MIDI Remote Scripts.  Your final directory path should be like this: MIDI Remote Scripts/LiveOSC/LiveOSC.py

Open Ableton Live and go to preferences.  Select MIDI preferences and click on an empty Control Surface dropdown.  Select LiveOSC in the dropdown.  LiveOSC is now setup and ready to use.

Note: As of version 8.3.3 on Windows you will need to install Python 2.5.1 for LiveOSC to work.  Download it at http://www.python.org/download/releases/2.5.1/.  This is due to the apparent removal of the socket library from Ableton's embedded Python interpreter.

Basic Usage Instructions
========================

There are many ways to use Pages but here's a few common use cases and instructions on how to configure them:

* Setup MIDI clock sync from Ableton Live to Pages

Many page types depend on MIDI clock sync so it's important to have it configured for things to work right.  To do this:

1. Launch Ableton Live
2. Go to Options -> Preferences -> MIDI / Sync tab.
3. Verify that you can see your virtual MIDI devices, if you don't see them listed then double check your virtual MIDI device settings.
4. Choose one device for MIDI Clock Sync. I generally use the first device (ie. MIDI Yoke 1, IAC Device 1, etc).
5. You will see two entries listed for the device, one labeled "Input:" and one labeled "Output:". Notice that each device has 3 buttons, "Track", "Sync", and "Remote".
6. For the Input: device, make sure all 3 buttons are turned off.
7. For the Output: device, make sure just the Sync button is turned on.
8. On the Audio tab of the preferences pane you will see an "Overall Latency" value. Make note of this value.
9. On the MIDI / Sync tab of the preferences pane click on the small gray arrow to the left of the Output: <virtual device> entry--the one that has Sync enabled. This should expand the entry and give you a few configuration options.
10. Set the MIDI Clock Sync Delay value to be -negative- the "Overall Latency" value from the Audio page, or as close as you can get to it. For example, if your Overall Latency is 24.7ms, you should set the MIDI Clock Sync Delay to -25ms.

Now launch pages, go to the MIDI menu -> MIDI In -> select the device you configured above.  You should now have MIDI clock sync from Ableton Live to pages.  You can create a MIDI Sequencer page to test it, when you hit play in Ableton you should see a vertical bar move across the monome.

* Use pages to play an instrument in Ableton Live

Page types like the MIDI Sequencer and MIDI Keyboard were meant to have something on the other side receiving their output.  To set this up:

1. In the Ableton Live's Preferences / MIDI tab, locate the devices you want Ableton to receive MIDI note and control change messages on.
2. Notice that there are two entries for each device, Input and Output. Make sure that all 3 buttons are disabled on all Output entries for the devices you want to receive MIDI notes and control change messages on.
3. Enable Track and Remote on the Input entries of the devices you wish to use. You can enable both Track and Remote for a device if you want both types of messages to go through it, or just one or the other.

In pages go to the MIDI menu -> MIDI Out -> select the device you configured above.  In the MIDI Sequencer/Keyboard page, click the 'Add MIDI Output' button and select the device.  The page should now send MIDI on the selected device.

* Route MLR in through an external application page

Routing in external applications can be tricky because most applications want to use the 8000/8080 ports to talk directly with Monome Serial.  Pages sits in between Monome Serial and other applications, so we need to configure the external application to talk to pages through an external application page instead of to Monome Serial directly.  An easy way to get one external application page running is as follwos:

1. Set Monome Serial to ports 7000 / 7070. This frees up MLR and the external application page to use 8000 / 8080, which are MLR's defaults.
2. Create an external application page. The default settings should work: prefix /mlr, hostname localhost, OSC in port 8080, OSC out port 8000.

If you want to route in other applications you'll need to set the ports appropriately on an External Application Page and the external application itself.

* Using the built-in pattern recorders

Each page has 8 or 16 patterns, depending on the size of the monome. Patterns can be accessed by holding down the page change button (the bottom right button). The top row of buttons each represent one pattern. When you hold down the page change button and press a pattern button you will trigger that pattern to play/record. The currently playing pattern is always recording all button presses even after it begins looping, and it will flash when the page change button is held down. Pushing another pattern button will switch the pattern that is running. If you press the currently running pattern button it will clear and disable the pattern, allowing you to record over it again.

By default, pattern recording is quantized to 1/16th notes. You can change the quantization setting for each page's patterns in the Page menu. You can also set the length of each page's patterns to a number of bars between 1 and 16. By default, patterns are 4 bars long. The pattern configuration will be saved with your configuration files, but the recorded patterns will not.

Patterns run off of MIDI clock signal so you must have a MIDI input selected and that MIDI input must be receiving MIDI clock from Ableton or another source. 
