import weka.core.Attribute;
import weka.core.Instance;

import java.util.Map;

/**
 * Created by mhorowitzgelb on 2/13/2015.
 */
public class ConditionalProbability {
    public Map<Attribute, String> valueMap;
    public double probability;

    public ConditionalProbability(Map<Attribute, String> valueMap, double probability){
        this.valueMap = valueMap;
        this.probability = probability;
    }

    public boolean Matches(Instance instance){
        for(Attribute attribute : valueMap.keySet()){
            if(!instance.stringValue(attribute).equals(valueMap.get(attribute)))
                return false;
        }
        return true;
    }

}
