package server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mpt.dictionary.MPTDictionaryDelta;
import mpt.dictionary.MPTDictionaryFull;
import mpt.dictionary.MPTDictionaryPartial;
import serialization.BVerifyAPIMessageSerialization.Updates;
import serialization.MptSerialization.MerklePrefixTrie;

public class ServerADSManager {
	
	private final String base;
	
	// current authentication information
	// over client ADSes
	private MPTDictionaryFull ads;
	
	// we also store the changes
	// TODO: consider if we want to store these 
	// or some subset of them on disk
	private List<MPTDictionaryDelta> deltas;
	
	// we keep a log of previous (key, value) mappings
	// so that if we need to abort a commit they
	// can be undone
	private List<Map.Entry<byte[], byte[]>> undoLog;
	
	public ServerADSManager(String base) {
		this.base = base;
		this.ads = new MPTDictionaryFull();
		this.undoLog = new ArrayList<>();
	}
	
	public void preCommitChange(byte[] key, byte[] value) {
		byte[] currentValue = this.ads.get(value);
		// save the old mapping in the undo log
		this.undoLog.add(Map.entry(key, currentValue));
		// make the change
		this.ads.insert(key, value);
	}
	
	public boolean commitChanges() {
		// save delta
		MPTDictionaryDelta delta = new MPTDictionaryDelta(this.ads);
		this.deltas.add(delta);
		// clear the changes
		this.ads.reset();
		this.undoLog.clear();
		return true;
	}
	
	public boolean abortChanges() {
		for(Map.Entry<byte[], byte[]> oldkv : this.undoLog) {
			byte[] key = oldkv.getKey();
			byte[] oldvalue = oldkv.getValue();
			this.undoChange(key, oldvalue);
		}
		return true;
	}
	
	private void undoChange(byte[] key, byte[] prevValue) {
		if(prevValue == null) {
			this.ads.delete(key);
		}else {
			this.ads.insert(key, prevValue);
		}
	}
	
	public byte[] get(byte[] key) {
		return this.ads.get(key);
	}
	
	public MerklePrefixTrie getProof(List<byte[]> keys) {
		MPTDictionaryPartial partial = new MPTDictionaryPartial(this.ads, keys);
		return partial.serialize();
	}
		
	public byte[] getUpdate(int startingCommitNumber, 
			List<byte[]> keyHashes) {
		Updates.Builder updates = Updates.newBuilder();
		// go through each commitment 
		for(int commitmentNumber = startingCommitNumber ; 
				commitmentNumber < this.deltas.size();
				commitmentNumber++) {
			// get the changes
			MPTDictionaryDelta delta = this.deltas.get(commitmentNumber);
			// and calculate the updates
			MerklePrefixTrie update = delta.getUpdates(keyHashes);
			updates.addUpdate(update);
		}
		return updates.build().toByteArray();
	}
	
}