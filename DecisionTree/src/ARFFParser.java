import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by maxhorowitz on 1/24/15.
 */
public class ARFFParser {
    public String relationName;
    public List<Attribute> attributes;
    public List<String[]>instances;
    public ARFFParser(String file) throws FileNotFoundException, ParseException {
        attributes = new ArrayList<Attribute>();
        instances = new ArrayList<String[]>();
        FileInputStream stream = new FileInputStream(file);
        Scanner scanner = new Scanner(stream);
        relationName = scanner.nextLine().split(" ")[1];
        String line;
        int index = 0;
        while(scanner.hasNextLine()){
            line = scanner.nextLine();
            if(line.contains("@data")){
                break;
            }
            else if(line.contains("@attribute")){
                Attribute attribute = new Attribute();
                attribute.name = line.split(" ")[1].split("'")[1];
                attribute.index = index;
                if(line.split(" ")[2].equals("real")){
                    attribute.type = Type.Real;
                }
                else {
                    attribute.type = Type.Nominal;
                    String a = line.split("[{] ")[1];
                    a = a.substring(0,a.length() -1);
                    attribute.nominalValues = a.split(", ");
                }
                attributes.add(attribute);
            }
            else{
                throw new ParseException("Not in ARFF format",0);
            }
            index ++;
        }
        while(scanner.hasNextLine()){
            line = scanner.nextLine();
            String[] array = line.split(",");
            if(array.length != attributes.size()){
                throw new ParseException("incorrect instance length",0);
            }
            instances.add(array);
        }

    }

    public static enum Type{
        Real,
        Nominal;
    }

    public static class Attribute{
        public int index;
        public String name;

        public Type type;
        public String[] nominalValues;
    }


}
