package it.unitn.disi.smatch.oracles.uby;

import static it.unitn.disi.smatch.oracles.uby.SmuUtils.checkNotEmpty;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;


/**
 * Statistics about edge insertions into the SynsetRelation graph.
 * 
 * @since 0.1
 *
 */
class InsertionStats {
    private Map<String, Long> map;
    
    
    public InsertionStats(){
        this.map = new HashMap();
    }

    /**
     * Increments provided relation name
     */
    public void inc(String relName){
        checkNotEmpty(relName, "Invalid key!");
        if (map.containsKey(relName)){
            map.put(relName, map.get(relName) + 1);
        } else {
            map.put(relName, 1L);
        }
    }

    public Set<String> relNames() {
        return map.keySet();
    }
    
    public long count(String relName){
        Long ret = map.get(relName);
        if (ret == null){
            return 0L;
        } else {
            return ret;
        }
    }

    /**
     * Returns total number of added edges. 
     * 
     */
    public long totEdges() {
        long ret = 0;
        for (Long v : map.values()){
            ret += v;
        }
        return ret;
    }

    /**
     * Returns a nice report of the insertions.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
                
        long tot = totEdges();
        if (tot == 0){
            sb.append("   No edges were inserted. \n");
        } else {
            sb.append("   Inserted " + tot+ " edges:\n");
            for (String relName : relNames()){
                sb.append("        " + relName + ":   " + count(relName) + "\n");
            }
        }
        sb.append("\n");
        
        return sb.toString();
    }    
    
}