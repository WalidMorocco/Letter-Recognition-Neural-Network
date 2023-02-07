// NNfile class: parses neural network input files
// storing network parameters and optional training
// data in memory.


import java.io.*;
import java.net.URL;
import java.util.*;
//import java.lang.*;

class FileFormatException extends Exception {
  public FileFormatException(String str) {
    super(str);
  }
}

public class NNfile {
  public int NumLayers;
  public int NumNeuronsPerLayer[];
  public int NumInput, NumHidden, NumOutput, NumTraining;
  public int WeightFlag;
  public int SpecialFlag;
  public int BaseIndex, TopIndex;
  private double data[];

  public NNfile() {
    NumLayers=NumInput=NumHidden=NumOutput=0;
  }

  public NNfile(String input_file) {
    data = new double[40000];
    TopIndex = 0;
    FileInputStream is = null;
    try {
      is = new FileInputStream(input_file);
    } catch (Exception E) {
        System.out.println("can not open file " + input_file);
    }
    try {
      if (is != null)  ReadFile(is);  is.close();
    } catch (Exception E) {
        System.out.println("can not process file");
    }
    System.out.println("Done with ReadFile, calling ParseData...");
    ParseData();
    OutputStream f = null;
    System.out.println("Done with ParseData(), write output...");
  }

  void ParseData() {
    int k = 0;
    NumLayers = (int)data[k++];
    NumNeuronsPerLayer = new int[NumLayers];
    for (int i=0; i<NumLayers; i++)
        NumNeuronsPerLayer[i]=(int)data[k++];
    NumInput    = NumNeuronsPerLayer[0];
    NumHidden   = NumNeuronsPerLayer[1];
    NumOutput   = NumNeuronsPerLayer[2];
    WeightFlag  = (int)data[k++];
    if (WeightFlag==0) {
        // Make room in data array for any weights added in later:
        int NumW = NumInput*NumHidden + NumHidden*NumOutput;
        for (int i=TopIndex; i>6; i--)  {
           data[i+NumW] = data[i];
        }
        TopIndex += NumW;
    }
    SpecialFlag = (int)data[k++];
    NumTraining = (int)data[k++];
    BaseIndex = k;
  }

  // To get weights:
  public double GetW1(int input, int hidden) {
     if (WeightFlag==0) return 0.0f;
     return data[BaseIndex +
                 + input * NumHidden + hidden];
  }
  public double GetW2(int hidden, int output) {
     if (WeightFlag==0) return 0.0f;
     return data[BaseIndex +
                 + NumInput * NumHidden +
                 hidden * NumOutput + output];
  }

  // To set weights:
  public void SetW1(int input, int hidden, double x) {
     WeightFlag=1; // set this so save() will save weights
     data[BaseIndex +
          + input * NumHidden + hidden] = x;
  }
  public void SetW2(int hidden, int output, double x) {
     WeightFlag=1; // set this so save() will save weights
     data[BaseIndex +
          NumInput * NumHidden +
          + hidden * NumOutput + output] = x;
  }

  // To get any application specific data:
  public double GetSpecial(int i) {
     if (SpecialFlag==0) return 0.0f;
     return data[BaseIndex +
                 NumInput*NumHidden + NumHidden*NumOutput
                 + i];
  }

  // To add application specific data:
  public void AddSpecial(double x) {
    // Make room in data array for a new special data value:
    int index = BaseIndex + NumInput*NumHidden +
                NumHidden*NumOutput + SpecialFlag;
    for (int i=TopIndex-1; i>=index; i--)  {
       data[i+1] = data[i];
    }
    TopIndex++;

    data[index] = x;
    SpecialFlag++;
  }

  // To get training cases:
  public double GetInput(int training_case, int neuron_index) {
     return data[BaseIndex +
                 NumInput*NumHidden + NumHidden*NumOutput +
                 SpecialFlag +
                 training_case*(NumInput + NumOutput) +
                 neuron_index];
  }

  public double GetOutput(int training_case, int neuron_index) {
     return data[BaseIndex +
                 NumInput*NumHidden + NumHidden*NumOutput +
                 SpecialFlag +
                 training_case*(NumInput + NumOutput) +
                 NumInput +
                 neuron_index];
  }

  public void RemoveTraining(int num) {
     if (num < 0 || num >= NumTraining) {
        System.out.println("Error in RemoveTraining(" + num + ")");
        return;
     }
     int index = BaseIndex +
                 NumInput*NumHidden + NumHidden*NumOutput +
                 SpecialFlag +
                 num * (NumInput + NumOutput);
     for (int i=index; i<=TopIndex - NumInput - NumOutput; i++) {
        data[i] = data[i + NumInput + NumOutput];
     }
     TopIndex -= NumInput + NumOutput;
     NumTraining--;
  }
  public void AddTraining(double inputs[], double outputs[]) {
     for (int i=0; i<NumInput; i++)
        data[TopIndex++] = inputs[i];
     for (int o=0; o<NumOutput; o++)
        data[TopIndex++] = outputs[o];
     NumTraining++;
  }

  void ReadFile(InputStream inp)
          throws IOException, FileFormatException {
    System.out.println("Entered ReadFile");
    StreamTokenizer st = new StreamTokenizer(inp);
    st.commentChar('#');
    st.eolIsSignificant(false);
    st.parseNumbers();
    System.out.println("Before while" );
    process:
    while (true) {
      switch(st.nextToken()) {
        case StreamTokenizer.TT_EOL:
          System.out.println("EOF found");
        break process;
        case StreamTokenizer.TT_NUMBER:
            double x = (double)st.nval;
            data[TopIndex++] = x;
        break;
        default:
          System.out.println("Token (default):" + st.sval);
        break process;
      }
    }
    System.out.println("Done with while loop");
    inp.close();
    if (st.ttype!=StreamTokenizer.TT_EOF)
      throw new FileFormatException(st.toString());
  }

  public void Save(String save_file_name) {
    try {
      FileOutputStream f = new FileOutputStream(save_file_name);
      PrintStream ps = new PrintStream(f);
      ps.println("#  Neural network data written by NNfile\n");
      ps.println(NumLayers + "  # number of neuron layers");
      for (int i=0; i<NumLayers; i++) {
         ps.println(NumNeuronsPerLayer[i] +
                    "  # neurons in layer " + i);
      }
      ps.println("1  # weight flag"); // always write out weights
      ps.println(SpecialFlag + "  # special data flag");
      ps.println(NumTraining + "  # number of training cases in file");

      ps.println("\n# Input layer to hidden layer weights:\n");
      for (int i=0; i<NumInput; i++) {
        for (int h=0; h<NumHidden; h++) {
          ps.print(GetW1(i, h) + " ");
        }
        ps.print("\n");
      }
      ps.println("\n# Hidden layer to output layer weights:\n");
      for (int h=0; h<NumHidden; h++) {
        for (int o=0; o<NumOutput; o++) {
          ps.print(GetW2(h, o) + " ");
        }
        ps.print("\n");
      }
      if (SpecialFlag > 0) {
        ps.println("\n# Special network data:\n");
        for (int i=0; i<SpecialFlag; i++) {
          ps.println(GetSpecial(i) + " ");
        }
        ps.println("\n");
      }
      ps.println("\n# Training data:\n");
      for (int i=0; i<NumTraining; i++) {
         for (int j=0; j<NumInput; j++) {
            ps.print(GetInput(i,j) + " ");
         }
         ps.print("   ");
         for (int j=0; j<NumOutput; j++) {
            ps.print(GetOutput(i,j) + " ");
         }
         ps.println("");
      }
      System.out.println("Done writing to output file.");
      ps.close();
      f.close();
    } catch (Exception E) {
        System.out.println("can not process the file " + save_file_name);
    }
  }
  public static void main(String argv[]) {
    NNfile test = new NNfile("test.dat");
  }
}

