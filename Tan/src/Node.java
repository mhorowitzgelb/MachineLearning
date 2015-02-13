import weka.core.Attribute;
import weka.core.Instances;

import java.util.List;

/**
 * Created by mhorowitzgelb on 2/13/2015.
 */
public abstract class Node {
    public Attribute attribute;

    public abstract double GetProbability(Instances instances);
}
