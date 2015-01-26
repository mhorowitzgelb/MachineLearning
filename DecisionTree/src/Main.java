import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by maxhorowitz on 1/24/15.
 */
public class Main {
    public static void main(String[] args) throws FileNotFoundException, ParseException {
        ARFFParser trainParser = new ARFFParser("/Users/maxhorowitz/Desktop/heart_train.arff.txt");
        Split.m = 20;
        Split root = new Split();
        root.instancesRemaining = trainParser.instances;

        //root.InfoGain(parser.attributes.get(parser.attributes.size() - 2));
        root.ChooseBestSplit(trainParser.attributes);

        for (Split child : root.children) {
            child.Print(root.attributeToSplit, false, 0, 0);
        }
        ARFFParser testParser = new ARFFParser("/Users/maxhorowitz/Desktop/heart_test.arff");


        int correct = 0;
        for (String[] instance : testParser.instances) {
            for (int i = 0; i < instance.length - 1; i++) {
                System.out.print(instance[i] + " ");
            }
            String treeClassification = root.classify(instance);
            String actualClassification = instance[instance.length - 1];
            System.out.println("Classified: " + treeClassification + " Actually: " + actualClassification);
            if (actualClassification.equals(treeClassification)) {
                correct++;
            }
        }
        System.out.println(correct + " correct out of " + testParser.instances.size());


        // Create a chart:
        Chart2D chart = new Chart2D();
        chart.getAxisX().setAxisTitle(new IAxis.AxisTitle("Training Size"));
        chart.getAxisY().setAxisTitle(new IAxis.AxisTitle("Accuracy"));
        // Create an ITrace:
        ITrace2D minTrace = new Trace2DSimple();
        ITrace2D maxTrace = new Trace2DSimple();
        ITrace2D averageTrace = new Trace2DSimple();
        minTrace.setName("Minimum Accuracy");
        minTrace.setColor(Color.blue);
        averageTrace.setName("Average Accuracy");
        averageTrace.setColor(Color.green);
        maxTrace.setName("Maximum Accuracy");
        maxTrace.setColor(Color.red);
        // Add the trace to the chart. This has to be done before adding points (deadlock prevention):
        chart.addTrace(minTrace);
        chart.addTrace(maxTrace);
        chart.addTrace(averageTrace);

        // Part 2
        Split.m = 4;
        ARFFParser p2TrainParser = new ARFFParser("/Users/maxhorowitz/Desktop/heart_train.arff.txt");
        ARFFParser p2TestParser = new ARFFParser("/Users/maxhorowitz/Desktop/heart_test.arff");

        List<String[]> positiveInstances = new ArrayList<String[]>();
        List<String[]> negativeInstances = new ArrayList<String[]>();
        for (String[] instance : p2TrainParser.instances) {
            if (instance[instance.length - 1].equals("positive"))
                positiveInstances.add(instance);
            else
                negativeInstances.add(instance);
        }
        float ratio = positiveInstances.size() / (float) p2TrainParser.instances.size();

        int[] trainSizes = {25, 50, 100, 200};


        for (int i = 0; i < trainSizes.length; i++) {
            int trainSize = trainSizes[i];
            float maxAccuracy = Float.NEGATIVE_INFINITY;
            float minAccuracy = Float.POSITIVE_INFINITY;
            float accuracySum = 0;
            for (int k = 0; k < 10; k++) {
                List<String[]> positiveInstancesCopy = new ArrayList<String[]>(positiveInstances);
                List<String[]> negativeInstancesCopy = new ArrayList<String[]>(negativeInstances);
                List<String[]> trainingInstances = new ArrayList<String[]>();
                if (trainSize != 200) {
                    for (int j = 0; j < (int)(ratio * trainSize); j++) {
                        trainingInstances.add(positiveInstancesCopy.remove((int)(Math.random() * positiveInstancesCopy.size())));
                    }
                    int negativeSize = trainSize - trainingInstances.size();
                    for(int j = 0; j < negativeSize; j ++){
                        trainingInstances.add(negativeInstancesCopy.remove((int)(Math.random() * negativeInstancesCopy.size())));
                    }
                }
                else{
                    trainingInstances.addAll(positiveInstancesCopy);
                    trainingInstances.addAll(negativeInstancesCopy);
                }
                Split tree = new Split();
                tree.instancesRemaining = trainingInstances;
                tree.ChooseBestSplit(p2TestParser.attributes);
                float accuracy = TestAccuracy(tree, p2TestParser.instances);
                minAccuracy = Math.min(minAccuracy, accuracy);
                maxAccuracy = Math.max(maxAccuracy, accuracy);
                accuracySum += accuracy;
            }
            float averageAccuracy = accuracySum / 10;
            averageTrace.addPoint(trainSize, averageAccuracy);
            maxTrace.addPoint(trainSize, maxAccuracy);
            minTrace.addPoint(trainSize, minAccuracy);
        }


        // Make it visible:
        // Create a frame.
        JFrame frame = new JFrame("Part 2");
        // add the chart to the frame:
        frame.getContentPane().add(chart);
        frame.setSize(1000, 800);
        // Enable the termination button [cross on the upper right edge]:
        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
        frame.setVisible(true);


        int[] mSizes = {2,5,10,20};

        Chart2D mGraph = new Chart2D();
        ITrace2D mTrace = new Trace2DSimple();
        mGraph.addTrace(mTrace);
        mGraph.getAxisY().setAxisTitle(new IAxis.AxisTitle("Accuracy"));
        mGraph.getAxisX().setAxisTitle(new IAxis.AxisTitle("m"));
        for(int i = 0; i < mSizes.length; i ++){
            Split.m = mSizes[i];
            Split tree = new Split();
            tree.instancesRemaining = p2TrainParser.instances;
            tree.ChooseBestSplit(p2TestParser.attributes);
            mTrace.addPoint(Split.m, TestAccuracy(tree, p2TestParser.instances));
        }

        JFrame part3 = new JFrame("Part 3");
        // add the chart to the frame:
        part3.getContentPane().add(mGraph);
        part3.setSize(1000, 800);
        // Enable the termination button [cross on the upper right edge]:
        part3.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
        part3.setVisible(true);



    }

    private static float TestAccuracy(Split root, List<String[]> testSet) {
        int correct = 0;
        for(String[] instance : testSet){
            if(root.classify(instance).equals(instance[instance.length -1]))
                correct ++;
        }
        return correct / (float) testSet.size();
    }

}
