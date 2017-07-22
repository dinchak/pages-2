"""
# Copyright (C) 2007 Rob King (rob@re-mu.org)
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
# For questions regarding this module contact
# Rob King <rob@e-mu.org> or visit http://www.e-mu.org

This file contains all the current Live OSC callbacks. 

"""
import Live
import RemixNet
import OSC
import LiveUtils

class LiveOSCCallbacks:
   
    def __init__(self, oscServer):
        
        if oscServer:
            self.oscServer = oscServer
            self.callbackManager = oscServer.callbackManager
            self.oscClient = oscServer.oscClient
        else:
            return

        self.callbackManager.add(self.tempoCB, "/live/tempo")
        self.callbackManager.add(self.timeCB, "/live/time")
        self.callbackManager.add(self.nextCueCB, "/live/next/cue")
        self.callbackManager.add(self.prevCueCB, "/live/prev/cue")
        self.callbackManager.add(self.playCB, "/live/play")
        self.callbackManager.add(self.playContinueCB, "/live/play/continue")
        self.callbackManager.add(self.playSelectionCB, "/live/play/selection")
        self.callbackManager.add(self.playClipCB, "/live/play/clip")
        self.callbackManager.add(self.playSceneCB, "/live/play/scene")
        self.callbackManager.add(self.sceneCB, "/live/scene")
        self.callbackManager.add(self.stopCB, "/live/stop")
        self.callbackManager.add(self.stopClipCB, "/live/stop/clip")
        self.callbackManager.add(self.stopTrackCB, "/live/stop/track")
        self.callbackManager.add(self.nameSceneCB, "/live/name/scene")
        self.callbackManager.add(self.nameTrackCB, "/live/name/track")
        self.callbackManager.add(self.nameClipCB, "/live/name/clip")
        self.callbackManager.add(self.armTrackCB, "/live/arm")
        self.callbackManager.add(self.disarmTrackCB, "/live/disarm")
        self.callbackManager.add(self.muteTrackCB, "/live/mute")
        self.callbackManager.add(self.unmuteTrackCB, "/live/unmute")
        self.callbackManager.add(self.soloTrackCB, "/live/solo")
        self.callbackManager.add(self.unsoloTrackCB, "/live/unsolo")
        self.callbackManager.add(self.volumeCB, "/live/volume")
        self.callbackManager.add(self.panCB, "/live/pan")
        self.callbackManager.add(self.sendCB, "/live/send")
        self.callbackManager.add(self.pitchCB, "/live/pitch")
        self.callbackManager.add(self.trackJump, "/live/track/jump")
        self.callbackManager.add(self.trackInfoCB, "/live/track/info")
        self.callbackManager.add(self.undoCB, "/live/undo")
        self.callbackManager.add(self.redoCB, "/live/redo")
        self.callbackManager.add(self.playClipSlotCB, "/live/play/clipslot")
        self.callbackManager.add(self.viewTrackCB, "/live/track/view")
        self.callbackManager.add(self.overdubCB, "/live/overdub")
        self.callbackManager.add(self.stateCB, "/live/state")
        self.callbackManager.add(self.clipInfoCB, "/live/clip/info")

    def tempoCB(self, msg):
        """Called when a /live/tempo message is received.

        Messages:
        /live/tempo                 Request current tempo, replies with /live/tempo (float tempo)
        /live/tempo (float tempo)   Set the tempo, replies with /live/tempo (float tempo)
        """
        if len(msg) == 3:
            tempo = msg[2]
            LiveUtils.setTempo(tempo)
        self.oscServer.sendOSC("/live/tempo", LiveUtils.getTempo())
        
    def timeCB(self, msg):
        """Called when a /live/time message is received.

        Messages:
        /live/time                 Request current song time, replies with /live/time (float time)
        /live/time (float time)    Set the time , replies with /live/time (float time)
        """
        if len(msg) == 3:
            time = msg[2]
            LiveUtils.currentTime(time)
        self.oscServer.sendOSC("/live/time", LiveUtils.currentTime())
        
    def nextCueCB(self, msg):
        """Called when a /live/next/cue message is received.

        Messages:
        /live/next/cue              Jumps to the next cue point
        """
        LiveUtils.jumpToNextCue()
        
    def prevCueCB(self, msg):
        """Called when a /live/prev/cue message is received.

        Messages:
        /live/prev/cue              Jumps to the previous cue point
        """
        LiveUtils.jumpToPrevCue()
        
    def playCB(self, msg):
        """Called when a /live/play message is received.

        Messages:
        /live/play              Starts the song playing
        """
        LiveUtils.play()
        
    def playContinueCB(self, msg):
        """Called when a /live/play/continue message is received.

        Messages:
        /live/play/continue     Continues playing the song from the current point
        """
        LiveUtils.continuePlaying()
        
    def playSelectionCB(self, msg):
        """Called when a /live/play/selection message is received.

        Messages:
        /live/play/selection    Plays the current selection
        """
        LiveUtils.playSelection()
        
    def playClipCB(self, msg):
        """Called when a /live/play/clip message is received.

        Messages:
        /live/play/clip     (int track, int clip)   Launches clip number clip in track number track
        """
        if len(msg) == 4:
            track = msg[2]
            clip = msg[3]
            LiveUtils.launchClip(track, clip)
            
    def playSceneCB(self, msg):
        """Called when a /live/play/scene message is received.

        Messages:
        /live/play/scene    (int scene)     Launches scene number scene
        """
        if len(msg) == 3:
            scene = msg[2]
            LiveUtils.launchScene(scene)
            
    def sceneCB(self, msg):
        """Called when a /live/scene message is received.
        
        Messages:
        /live/scene         no argument or 'query'  Returns the currently playing scene number
        """
        if len(msg) == 2 or (len(msg) == 3 and msg[2] == "query"):
            selected_scene = LiveUtils.getSong().view.selected_scene
            scenes = LiveUtils.getScenes()
            index = 0
            selected_index = 0
            for scene in scenes:
                index = index + 1        
                if scene == selected_scene:
                    selected_index = index
            self.oscServer.sendOSC("/live/scene", (selected_index))
    
    def stopCB(self, msg):
        """Called when a /live/stop message is received.

        Messages:
        /live/stop              Stops playing the song
        """
        LiveUtils.stop()
        
    def stopClipCB(self, msg):
        """Called when a /live/stop/clip message is received.

        Messages:
        /live/stop/clip     (int track, int clip)   Stops clip number clip in track number track
        """
        if len(msg) == 4:
            track = msg[2]
            clip = msg[3]
            LiveUtils.stopClip(track, clip)

    def stopTrackCB(self, msg):
        """Called when a /live/stop/track message is received.

        Messages:
        /live/stop/track     (int track, int clip)   Stops track number track
        """
        if len(msg) == 3:
            track = msg[2]
            LiveUtils.stopTrack(track)

    def nameSceneCB(self, msg):
        """Called when a /live/name/scene message is received.

        Messages:
        /live/name/scene                            Rerurns a a series of all the scene names in the form /live/name/scene (int scene, string name)
        /live/name/scene    (int scene)             Returns a single scene's name in the form /live/name/scene (int scene, string name)
        /live/name/scene    (int scene, string name)Sets scene number scene's name to name

        """        
        #Requesting all scene names
        if len(msg) == 2:
            sceneNumber = 0
            for scene in LiveUtils.getScenes():
                self.oscServer.sendOSC("/live/name/scene", (sceneNumber, scene.name))
                sceneNumber = sceneNumber + 1
            return
        #Requesting a single scene name
        if len(msg) == 3:
            sceneNumber = msg[2]
            self.oscServer.sendOSC("/live/name/scene", (sceneNumber, LiveUtils.getScene(sceneNumber).name))
            return
        #renaming a scene
        if len(msg) == 4:
            sceneNumber = msg[2]
            name = msg[3]
            LiveUtils.getScene(sceneNumber).name = name
            
    def nameTrackCB(self, msg):
        """Called when a /live/name/track message is received.

        Messages:
        /live/name/track                            Rerurns a a series of all the track names in the form /live/name/track (int track, string name)
        /live/name/track    (int track)             Returns a single track's name in the form /live/name/track (int track, string name)
        /live/name/track    (int track, string name)Sets track number track's name to name

        """        
        #Requesting all track names
        if len(msg) == 2:
            trackNumber = 0
            for track in LiveUtils.getTracks():
                self.oscServer.sendOSC("/live/name/track", (trackNumber, track.name))
                trackNumber = trackNumber + 1
            return
        #Requesting a single track name
        if len(msg) == 3:
            trackNumber = msg[2]
            self.oscServer.sendOSC("/live/name/track", (trackNumber, LiveUtils.getTrack(trackNumber).name))
            return
        #renaming a track
        if len(msg) == 4:
            trackNumber = msg[2]
            name = msg[3]
            LiveUtils.getTrack(trackNumber).name = name

    def nameClipCB(self, msg):
        """Called when a /live/name/clip message is received.

        Messages:
        /live/name/clip                                      Rerurns a a series of all the clip names in the form /live/name/clip (int track, int clip, string name)
        /live/name/clip    (int track, int clip)             Returns a single clip's name in the form /live/name/clip (int clip, string name)
        /live/name/clip    (int track, int clip, string name)Sets clip number clip in track number track's name to name

        """        
        #Requesting all clip names
        if len(msg) == 2:
            trackNumber = 0
            clipNumber = 0
            for track in LiveUtils.getTracks():
                for clipSlot in track.clip_slots:
                    if clipSlot:
                        self.oscServer.sendOSC("/live/name/clip", (trackNumber, clipNumber, clipSlot.clip.name))
                    clipNumber = clipNumber + 1
                clipNumber = 0
                trackNumber = trackNumber + 1
            return
        #Requesting a single scene name
        if len(msg) == 4:
            trackNumber = msg[2]
            clipNumber = msg[3]
            self.oscServer.sendOSC("/live/name/scene", (trackNumber, clipNumber, LiveUtils.getClip(trackNumber, clipNumber).name))
            return
        #renaming a scene
        if len(msg) == 5:
            trackNumber = msg[2]
            clipNumber = msg[3]
            name = msg[4]
            LiveUtils.getClip(trackNumber, clipNumber).name = name
    
    def armTrackCB(self, msg):
        """Called when a /live/arm message is received.

        Messages:
        /live/arm     (int track)   (int armed/disarmed)     Arms track number track
        """
        track = msg[2]
        
        if len(msg) == 4:
            if msg[3] == 1:
                LiveUtils.armTrack(track)
            else:
                LiveUtils.disarmTrack(track)
        # Return arm status        
        elif len(msg) == 3:
            status = LiveUtils.getTrack(track).arm
            self.oscServer.sendOSC("/live/arm", (track, int(status))) 
            
    def disarmTrackCB(self, msg):
        """Called when a /live/disarm message is received.

        Messages:
        /live/disarm     (int track)   Disarms track number track
        """
        if len(msg) == 3:
            track = msg[2]
            LiveUtils.disarmTrack(track)
            
    def muteTrackCB(self, msg):
        """Called when a /live/mute message is received.

        Messages:
        /live/mute     (int track)   Mutes track number track
        """
        track = msg[2]
            
        if len(msg) == 4:
            if msg[3] == 1:
                LiveUtils.muteTrack(track)
            else:
                LiveUtils.unmuteTrack(track)
                
        elif len(msg) == 3:
            status = LiveUtils.getTrack(track).mute
            self.oscServer.sendOSC("/live/mute", (track, int(status)))
            
    def unmuteTrackCB(self, msg):
        """Called when a /live/unmute message is received.

        Messages:
        /live/unmute     (int track)   Unmutes track number track
        """
        if len(msg) == 3:
            track = msg[2]
            LiveUtils.unmuteTrack(track)
            
    def soloTrackCB(self, msg):
        """Called when a /live/solo message is received.

        Messages:
        /live/solo     (int track)   Solos track number track
        """
        track = msg[2]
        
        if len(msg) == 4:
            if msg[3] == 1:
                LiveUtils.soloTrack(track)
            else:
                LiveUtils.unsoloTrack(track)
            
        elif len(msg) == 3:
            status = LiveUtils.getTrack(track).solo
            self.oscServer.sendOSC("/live/solo", (track, int(status)))
            
    def unsoloTrackCB(self, msg):
        """Called when a /live/unsolo message is received.

        Messages:
        /live/unsolo     (int track)   Unsolos track number track
        """
        if len(msg) == 3:
            track = msg[2]
            LiveUtils.unsoloTrack(track)
            
    def volumeCB(self, msg):
        """Called when a /live/volume message is received.

        Messages:
        /live/volume     (int track)                            Returns the current volume of track number track as: /live/volume (int track, float volume(0.0 to 1.0))
        /live/volume     (int track, float volume(0.0 to 1.0))  Sets track number track's volume to volume
        """
        if len(msg) == 4:
            track = msg[2]
            volume = msg[3]
            LiveUtils.trackVolume(track, volume)
        if len(msg) >= 3:
            track = msg[2]
            self.oscServer.sendOSC("/live/volume", (track, LiveUtils.trackVolume(track)))
            
    def panCB(self, msg):
        """Called when a /live/pan message is received.

        Messages:
        /live/pan     (int track)                            Returns the pan of track number track as: /live/pan (int track, float pan(-1.0 to 1.0))
        /live/pan     (int track, float pan(-1.0 to 1.0))     Sets track number track's pan to pan

        """
        if len(msg) == 4:
            track = msg[2]
            pan = msg[3]
            LiveUtils.trackPan(track, pan)
        if len(msg) >= 3:
            track = msg[2]
            self.oscServer.sendOSC("/live/pan", (track, LiveUtils.trackPan(track)))
            
    def sendCB(self, msg):
        """Called when a /live/send message is received.

        Messages:
        /live/send     (int track, int send)                              Returns the send level of send (send) on track number track as: /live/send (int track, int send, float level(0.0 to 1.0))
        /live/send     (int track, int send, float level(0.0 to 1.0))     Sets the send (send) of track number (track)'s level to (level)

        """
        if len(msg) == 5:
            track = msg[2]
            send = msg[3]
            level = msg[4]
            LiveUtils.trackSend(track, volume)
        if len(msg) >= 4:
            track = msg[2]
            send = msg[3]
            self.oscServer.sendOSC("/live/volume", LiveUtils.trackSend(track, send))
            
    def pitchCB(self, msg):
        """Called when a /live/pitch message is received.

        Messages:
        /live/pitch     (int track, int clip)                                               Returns the pan of track number track as: /live/pan (int track, int clip, int coarse(-48 to 48), int fine (-50 to 50))
        /live/pitch     (int track, int clip, int coarse(-48 to 48), int fine (-50 to 50))  Sets clip number clip in track number track's pitch to coarse / fine

        """
        if len(msg) == 6:
            track = msg[2]
            clip = msg[3]
            coarse = msg[4]
            fine = msg[5]
            LiveUtils.clipPitch(track, clip, coarse, fine)
        if len(msg) >=4:
            track = msg[2]
            clip = msg[3]
            self.oscServer.sendOSC("/live/pitch", LiveUtils.clipPitch(track, clip))

    def trackJump(self, msg):
        """Called when a /live/track/jump message is received.

        Messages:
        /live/track/jump     (int track, float beats)   Jumps in track's currently running session clip by beats
        """
        if len(msg) == 4:
            track = msg[2]
            beats = msg[3]
            track = LiveUtils.getTrack(track)
            track.jump_in_running_session_clip(beats)

    def trackInfoCB(self, msg):
        """Called when a /live/track/info message is received.

        Messages:
        /live/track/info     (int track)   Returns clip slot status' for all clips in a track in the form /live/track/info (tracknumber, armed  (clipnumber, state, length))
                                           [state: 1 = Has Clip, 2 = Playing, 3 = Triggered]
        """
        
        clipslots = LiveUtils.getClipSlots()
        
        new = []
        if len(msg) == 3:
            new.append(clipslots[msg[2]])
            tracknum = msg[2] - 1
        else:
            new = clipslots
            tracknum = -1
        
        for track in new:
            tracknum = tracknum + 1
            clipnum = -1
            tmptrack = LiveUtils.getTrack(tracknum)
            armed = tmptrack.arm and 1 or 0
            li = [tracknum, armed]
            for clipSlot in track:
                clipnum = clipnum + 1
                li.append(clipnum);
                if clipSlot.clip != None:
                    clip = clipSlot.clip
                    if clip.is_playing == 1:
                        li.append(2)
                        clipLength = clip.loop_end - clip.loop_start
                        li.append(clipLength)
                        
                    elif clip.is_triggered == 1:
                        li.append(3)
                        li.append(0.0)
                        
                    else:
                        li.append(1)
                        li.append(0.0)
                else:
                    li.append(0)
                    li.append(0.0)
                    
            tu = tuple(li)
            
            self.oscServer.sendOSC("/live/track/info", tu)


    def undoCB(self, msg):
        """Called when a /live/undo message is received.
        
        Messages:
        /live/undo      Requests the song to undo the last action
        """
        LiveUtils.getSong().undo()
        
    def redoCB(self, msg):
        """Called when a /live/redo message is received.
        
        Messages:
        /live/redo      Requests the song to redo the last action
        """
        LiveUtils.getSong().redo()
        
    def playClipSlotCB(self, msg):
        """Called when a /live/play/clipslot message is received.
        
        Messages:
        /live/play/clipslot     (int track, int clip)   Launches clip number clip in track number track
        """
        ml = len(msg)
        if len(msg) == 4:
            track_num = msg[2]
            clip_num = msg[3]
            track = LiveUtils.getTrack(track_num)
            clipslot = track.clip_slots[clip_num]
            clipslot.fire()
            
    def viewTrackCB(self, msg):
        if len(msg) == 3:
            track_num = msg[2]
            track = LiveUtils.getTrack(track_num)
            track.view.select_instrument()

    def overdubCB(self, msg):
        if len(msg) == 3:
            overdub = msg[2]
            LiveUtils.getSong().overdub = overdub

    def stateCB(self, msg):
        tempo = LiveUtils.getTempo()
        overdub = LiveUtils.getSong().overdub
        selected_scene = LiveUtils.getSong().view.selected_scene
        scenes = LiveUtils.getScenes()
        index = 0
        selected_index = 0
        for scene in scenes:
            index = index + 1        
            if scene == selected_scene:
                selected_index = index
        self.oscServer.sendOSC("/live/state", (tempo, overdub, selected_index))

    def clipInfoCB(self,msg):
        """Called when a /live/clip/info message is received.
        
        Messages:
        /live/clip/info     (int track, int clip)      Gets the status of a single clip in the form  /live/clip/info (tracknumber, clipnumber, state)
                                                       [state: 1 = Has Clip, 2 = Playing, 3 = Triggered]
        """
        
        if len(msg) == 4:
            trackNumber = msg[2]
            clipNumber = msg[3]    
            
            clip = LiveUtils.getClip(trackNumber, clipNumber)
            
            playing = 0
            
            if clip != None:
                playing = 1
                
                if clip.is_playing == 1:
                    playing = 2
                elif clip.is_triggered == 1:
                    playing = 3

            self.oscServer.sendOSC("/live/clip/info", (trackNumber, clipNumber, playing))
        
        return