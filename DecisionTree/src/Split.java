import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import ARFFParser.Attribute;
import ARFFParser.Type;
/**
 * Created by maxhorowitz on 1/24/15.
 */
public class Split {
    public int r_val;
    public String s_val;
    public List<Split> children;
    public List<String[]> instancesRemaining;
    public Attribute attributeToSplit;
    public String classVal;

    public void ChooseBestSplit(List<Attribute> attributes){
        float bestInfoGain = Float.MIN_VALUE;
        for(Attribute attribute : attributes){
            if(attribute.type == Type.Nominal){
                float infoGain = InfoGain(attribute);
                if(infoGain > bestInfoGain){
                    //bestInfoGain = infoGain;
                    //infoGain
                }
            }
            else{
                Collections.sort(instancesRemaining, new InstanceComparator(attribute.index));
                for(int i = 0; i < instancesRemaining.size() - 1; i ++) {
                    //infoGain = InfoGain(attribute, threshold);
                }
            }
        }
    }

    public float InfoGain(Attribute attribute){
        if(attribute.type == Type.Nominal){

        }
        else{
            Collections.sort(instancesRemaining, new InstanceComparator(attribute.index));
            for(int i = 0; i < instancesRemaining.size() -1; i ++) {
                return InfoGain()
            }
        }
    }

    public float InfoGain(Attribute attribute, float threshold){

    }

    public static class InstanceComparator implements Comparator<String[]>{

        private int index;
        public InstanceComparator(int index){
            this.index = index;
        }
        @Override
        public int compare(String[] o1, String[] o2) {
            float a = Float.parseFloat(o1[index]);
            float b = Float.parseFloat(o2[index]);
            if(a > b)
                return 1;
            else if(a < b)
                return 0;
            else return -1;
        }
    }
}
