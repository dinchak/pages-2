"""
# Copyright (C) 2007 Nathan Ramella (nar@remix.net)
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
# Nathan Ramella <nar@remix.net> or visit http://www.remix.net

This script is based off the Ableton Live supplied MIDI Remote Scripts, customised
for OSC request delivery and response. This script can be run without any extra
Python libraries out of the box. 

This is the second file that is loaded, by way of being instantiated through
__init__.py

"""

import Live
import socket
import LiveOSCCallbacks
import RemixNet
import OSC
import LiveUtils

class LiveOSC:
    __module__ = __name__
    __doc__ = "Main class that establishes the LiveOSC Component"

    _LOG = 0

    clisten = {}
    slisten = {}
    mlisten = { "solo": {}, "mute": {}, "arm": {}, "panning": {}, "volume": {}, "sends": {} }
    scenelisten = {}
    triglisten = {}
    
    def __init__(self, c_instance):
        self._LiveOSC__c_instance = c_instance
      
        self.basicAPI = 0       
        self.oscServer = RemixNet.OSCServer('localhost')
        self.oscServer.sendOSC('/remix/oscserver/startup', 1)
        
    def disconnect(self):
        self.oscServer.sendOSC('/remix/oscserver/shutdown', 1)
        self.oscServer.shutdown()

    def connect_script_instances(self, instanciated_scripts):
        """
        Called by the Application as soon as all scripts are initialized.
        You can connect yourself to other running scripts here, as we do it
        connect the extension modules
        """
        return

    def getOSCServer(self):
        return self.oscServer
    
    def application(self):
        """returns a reference to the application that we are running in"""
        return Live.Application.get_application()

    def song(self):
        """returns a reference to the Live Song that we do interact with"""
        return self._LiveOSC__c_instance.song()

    def handle(self):
        """returns a handle to the c_interface that is needed when forwarding MIDI events via the MIDI map"""
        return self._LiveOSC__c_instance.handle()

    def refresh_state(self):
        """..."""
        return

    def is_extension(self):
        return False

    def request_rebuild_midi_map(self):
        """
        To be called from any components, as soon as their internal state changed in a 
        way, that we do need to remap the mappings that are processed directly by the 
        Live engine.
        Dont assume that the request will immediately result in a call to
        your build_midi_map function. For performance reasons this is only
        called once per GUI frame.
        """
        return

    def build_midi_map(self, midi_map_handle):
        """
        New MIDI mappings can only be set when the scripts 'build_midi_map' function 
    	is invoked by our C instance sibling. Its either invoked when we have requested it 
    	(see 'request_rebuild_midi_map' above) or when due to a change in Lives internal state,
    	a rebuild is needed.
        """
        return
    
    def update_display(self):
        """
        This function is run every 100ms, so we use it to initiate our Song.current_song_time
        listener to allow us to process incoming OSC commands as quickly as possible under
        the current listener scheme.
        """
        ######################################################
        # START OSC LISTENER SETUP
              
        if self.basicAPI == 0:
            # By default we have set basicAPI to 0 so that we can assign it after
            # initialization. We try to get the current song and if we can we'll
            # connect our basicAPI callbacks to the listener allowing us to 
            # respond to incoming OSC every 60ms.
            #
            # Since this method is called every 100ms regardless of the song time
            # changing, we use both methods for processing incoming UDP requests
            # so that from a resting state you can initiate play/clip triggering.
            
            try:
                doc = self.song()
            except:
                return
            try:
                self.basicAPI = LiveOSCCallbacks.LiveOSCCallbacks(self.oscServer)
                doc.add_current_song_time_listener(self.oscServer.processIncomingUDP)
                self.oscServer.sendOSC('/remix/echo', 'basicAPI setup complete')
            except:
                return
            
            # If our OSC server is listening, try processing incoming requests.
            # Any 'play' initiation will trigger the current_song_time listener
            # and bump updates from 100ms to 60ms.
            
        if self.oscServer:
            try:
                self.oscServer.processIncomingUDP()
            except:
                pass
            
        # END OSC LISTENER SETUP
        ######################################################

    def send_midi(self, midi_event_bytes):
        """
        Use this function to send MIDI events through Live to the _real_ MIDI devices 
        that this script is assigned to.
        """
        pass

    def receive_midi(self, midi_bytes):
        return

    def can_lock_to_devices(self):
        return False

    def suggest_input_port(self):
        return ''

    def suggest_output_port(self):
        return ''

    def __handle_display_switch_ids(self, switch_id, value):
	pass

######################################################################
# Useful Methods

    def getOSCServer(self):
        return self.oscServer
    
    def application(self):
        """returns a reference to the application that we are running in"""
        return Live.Application.get_application()

    def song(self):
        """returns a reference to the Live Song that we do interact with"""
        return self._LiveOSC__c_instance.song()

    def handle(self):
        """returns a handle to the c_interface that is needed when forwarding MIDI events via the MIDI map"""
        return self._LiveOSC__c_instance.handle()
    def log(self, msg):
        if self._LOG == 1:
            self.logger.log(msg) 
            
    def getslots(self):
        tracks = self.song().tracks

        clipSlots = []
        for track in tracks:
            clipSlots.append(track.clip_slots)
        return clipSlots
        
######################################################################
# Used Ableton Methods

    def disconnect(self):
        self.rem_clip_listeners()
        self.rem_mixer_listeners()
        self.rem_scene_listener()
	self.rem_tempo_listener()
	self.rem_overdub_listener()
	self.rem_tracks_listener()
        self.oscServer.sendOSC('/remix/oscserver/shutdown', 1)
        self.oscServer.shutdown()
            
    def build_midi_map(self, midi_map_handle):
        self.refresh_state()            
            
    def refresh_state(self):
        self.add_clip_listeners()
        self.add_mixer_listeners()
        self.add_scene_listener()
	self.add_tempo_listener()
	self.add_overdub_listener()
	self.add_tracks_listener()

        trackNumber = 0
        clipNumber = 0
        for track in self.song().tracks:
            self.oscServer.sendOSC("/live/name/track", (trackNumber, str(track.name)))
            
            for clipSlot in track.clip_slots:
                if clipSlot.clip != None:
                    self.oscServer.sendOSC("/live/name/clip", (trackNumber, clipNumber, str(clipSlot.clip.name), str(clipSlot.clip.color_index)))
                clipNumber = clipNumber + 1
            clipNumber = 0
            trackNumber = trackNumber + 1        

######################################################################
# Add / Remove Listeners

    def add_tracks_listener(self):
	if self.song().tracks_has_listener(self.tracks_change) != 1:
	    self.song().add_tracks_listener(self.tracks_change)
    
    def rem_tracks_listener(self):
	if self.song().tracks_has_listener(self.tempo_change) == 1:
	    self.song().remove_tracks_listener(self.tracks_change)
    
    def tracks_change(self):
	self.oscServer.sendOSC("/live/refresh", (1))

    def add_tempo_listener(self):
	if self.song().tempo_has_listener(self.tempo_change) != 1:
	    self.song().add_tempo_listener(self.tempo_change)
	    
    def rem_tempo_listener(self):
	if self.song().tempo_has_listener(self.tempo_change) == 1:
	    self.song().remove_tempo_listener(self.tempo_change)
	    
    def tempo_change(self):
	tempo = LiveUtils.getTempo()
	self.oscServer.sendOSC("/live/tempo", (tempo))
	
    def add_overdub_listener(self):
	if self.song().overdub_has_listener(self.overdub_change) != 1:
	    self.song().add_overdub_listener(self.overdub_change)
	    
    def rem_overdub_listener(self):
	if self.song().overdub_has_listener(self.overdub_change) == 1:
	    self.song().remove_overdub_listener(self.overdub_change)
	    
    def overdub_change(self):
	overdub = LiveUtils.getSong().overdub
	self.oscServer.sendOSC("/live/overdub", (int(overdub) + 1))

    def add_scene_listener(self):
        if self.song().view.selected_scene_has_listener(self.scene_change) != 1:
            self.song().view.add_selected_scene_listener(self.scene_change)

    def rem_scene_listener(self):
        if self.song().view.selected_scene_has_listener(self.scene_change) == 1:
            self.song().view.remove_selected_scene_listener(self.scene_change)

    def scene_change(self):
        selected_scene = self.song().view.selected_scene
        scenes = self.song().scenes
        index = 0
        selected_index = 0
        for scene in scenes:
            index = index + 1        
            if scene == selected_scene:
                selected_index = index
        self.oscServer.sendOSC("/live/scene", (selected_index))
    	    
    def rem_clip_listeners(self):
        self.log("** Remove Listeners **")
    
        for slot in self.slisten:
            if slot != None:
                if slot.has_clip_has_listener(self.slisten[slot]) == 1:
                    slot.remove_has_clip_listener(self.slisten[slot])
    
        self.slisten = {}
        
        for clip in self.clisten:
            if clip != None:
                if clip.is_playing_has_listener(self.clisten[clip]) == 1:
                    clip.remove_is_playing_listener(self.clisten[clip])
		    
        for clip in self.triglisten:
            if clip != None:
                if clip.is_triggered_has_listener(self.triglisten[clip]) == 1:
                    clip.remove_is_triggered_listener(self.triglisten[clip])
                
        self.triglisten = {}
        
    def add_clip_listeners(self):
        self.rem_clip_listeners()
    
        tracks = self.getslots()
        for track in range(len(tracks)):
            for clip in range(len(tracks[track])):
                c = tracks[track][clip]
                if c.clip != None:
                    self.add_cliplistener(c.clip, track, clip)
                    self.log("ClipLauncher: added clip listener tr: " + str(track) + " clip: " + str(clip));
                
                else:
                    self.add_slotlistener(c, track, clip)
        
    def add_cliplistener(self, clip, tid, cid):
        cb = lambda :self.clip_changestate(clip, tid, cid)
	cb2 = lambda :self.clip_triggered(clip, tid, cid)
        
        if self.clisten.has_key(clip) != 1:
            clip.add_is_playing_listener(cb)
	    clip.add_is_triggered_listener(cb2)
            self.clisten[clip] = cb
	    self.triglisten[clip] = cb2
        
    def add_slotlistener(self, slot, tid, cid):
        cb = lambda :self.slot_changestate(slot, tid, cid)
        
        if self.slisten.has_key(slot) != 1:
            slot.add_has_clip_listener(cb)
            self.slisten[slot] = cb   
            
    
    
    def rem_mixer_listeners(self):
        for type in ("arm", "solo", "mute"):
            for tr in self.mlisten[type]:
                cb = self.mlisten[type][tr]
                
                test = eval("tr." + type+ "_has_listener(cb)")
                
                if test == 1:
                    eval("tr.remove_" + type + "_listener(cb)")
                
        for type in ("volume", "panning"):
            for tr in self.mlisten[type]:
                cb = self.mlisten[type][tr]
                
                test = eval("tr.mixer_device." + type+ ".value_has_listener(cb)")
                
                if test == 1:
                    eval("tr.mixer_device." + type + ".remove_value_listener(cb)")
         
        for tr in self.mlisten["sends"]:
            for send in self.mlisten["sends"][tr]:
                cb = self.mlisten["sends"][tr][send]
                
                test = eval("tr.mixer_device.sends[" + str(send) + "].value_has_listener(cb)")
                
                if test == 1:
                    eval("tr.mixer_device.sends[" + str(send) + "].remove_value_listener(cb)")    
    
    def add_mixer_listeners(self):
        self.rem_mixer_listeners()
        
        tracks = self.song().tracks
        for track in range(len(tracks)):
            tr = tracks[track]
            
            for type in ("arm", "solo", "mute"):
                self.add_mixert_listener(track, type, tr)
                
            for type in ("volume", "panning"):
                self.add_mixerv_listener(track, type, tr)
                
            for sid in range(len(tr.mixer_device.sends)):
                self.add_send_listener(track, tr, sid)
        
        
    def add_send_listener(self, tid, track, sid):
        if self.mlisten["sends"].has_key(track) != 1:
            self.mlisten["sends"][track] = {}
                    
        if self.mlisten["sends"][track].has_key(sid) != 1:
            cb = lambda :self.send_changestate(tid, track, sid)
            
            self.mlisten["sends"][track][sid] = cb
            eval("track.mixer_device.sends[" + str(sid) + "].add_value_listener(cb)")
    
    def add_mixert_listener(self, tid, type, track):
        if self.mlisten[type].has_key(track) != 1:
            cb = lambda :self.mixert_changestate(type, tid, track)
            
            self.mlisten[type][track] = cb
            eval("track.add_" + type + "_listener(cb)")
            
    def add_mixerv_listener(self, tid, type, track):
        if self.mlisten[type].has_key(track) != 1:
            cb = lambda :self.mixerv_changestate(type, tid, track)
            
            self.mlisten[type][track] = cb
            eval("track.mixer_device." + type + ".add_value_listener(cb)")
               

######################################################################
# Listener Callbacks
        
    # Clip Callbacks
    def slot_changestate(self, slot, tid, cid):
        # Added new clip
	print "added new clip"
        tmptrack = LiveUtils.getTrack(tid)
        armed = tmptrack.arm and 1 or 0
        if slot.clip != None:
            self.add_cliplistener(slot.clip, tid, cid)
            
            playing = 1
            if slot.clip.is_playing == 1:
                playing = 2
            
            if slot.clip.is_triggered == 1:
                playing = 3
            
            length =  slot.clip.loop_end - slot.clip.loop_start
            
            
            self.oscServer.sendOSC('/live/track/info', (tid, armed, cid, playing, length))
        else:
            self.oscServer.sendOSC('/live/track/info', (tid, armed, cid, 0, 0.0))
            if self.clisten.has_key(slot.clip) == 1:
                slot.clip.remove_playing_status_listener(self.clisten[slot.clip])
                
        #self.log("Slot changed" + str(self.clips[tid][cid]))
    
    def clip_changestate(self, clip, x, y):
        self.log("Listener: x: " + str(x) + " y: " + str(y));

        playing = 1
        
        if clip.is_playing == 1:
            playing = 2
            
        if clip.is_triggered == 1:
            playing = 3
            
        self.oscServer.sendOSC('/live/clip/info', (x, y, playing))
        
        #self.log("Clip changed x:" + str(x) + " y:" + str(y) + " status:" + str(playing))
	
    def clip_triggered(self, clip, x, y):
	self.oscServer.sendOSC('/live/clip/info', (x, y, 3))
        
        
    # Mixer Callbacks
        
    def mixerv_changestate(self, type, tid, track):
        val = eval("track.mixer_device." + type + ".value")
        types = { "panning": "pan", "volume": "vol" }
        self.oscServer.sendOSC('/live/' + types[type], (tid, float(val)))        
        
    def mixert_changestate(self, type, tid, track):
        val = eval("track." + type)
        self.oscServer.sendOSC('/live/' + type, (tid, int(val)))        
    
    def send_changestate(self, tid, track, sid):
        val = eval("track.mixer_device.sends[" + str(sid) + "].value")
        self.oscServer.sendOSC('/live/send', (tid, sid, float(val)))   