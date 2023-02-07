// Neural Network Java classes
//


import java.awt.*;
import java.applet.Applet;
import java.lang.*;
import java.util.*;

class Neural extends Object {

   // For debug output:
   GUI MyGUI = null;

   protected int NumInputs;
   protected int NumHidden;
   protected int NumOutputs;

   protected int NumTraining;
   protected int WeightsFlag;
   protected int SpecialFlag;

   public double Inputs[];
   protected double Hidden[];
   public double Outputs[];

   protected double W1[][];
   protected double W2[][];

   protected double output_errors[];
   protected double hidden_errors[];

   protected double InputTraining[];
   protected double OutputTraining[];

   // mask of training examples to ignore (true -> ignore):
   public boolean IgnoreTraining[] = null;
   // mask of Input neurons to ignore:
   public boolean IgnoreInput[] = null;
    public NNfile NeuralFile=null;

   Neural() {
    NumInputs = NumHidden = NumOutputs = 0;
   }

   Neural(String file_name) {
     NeuralFile = new NNfile(file_name);
     NumInputs = NeuralFile.NumInput;
     NumHidden = NeuralFile.NumHidden;
     NumOutputs = NeuralFile.NumOutput;
     NumTraining= NeuralFile.NumTraining;
     WeightsFlag= NeuralFile.WeightFlag;
     SpecialFlag= NeuralFile.SpecialFlag;

     Inputs = new double[NumInputs];
     Hidden = new double[NumHidden];
     Outputs = new double[NumOutputs];
     W1 = new double[NumInputs][NumHidden];
     W2 = new double[NumHidden][NumOutputs];
     // Retrieve the weight values from the NNfile object:
     if (WeightsFlag!=0) {
       for (int i=0; i<NumInputs; i++) {
          for (int h=0; h<NumHidden; h++) {
            W1[i][h] = NeuralFile.GetW1(i, h);
          }
       }
       for (int h=0; h<NumHidden; h++) {
          for (int o=0; o<NumOutputs; o++) {
            W2[h][o] = NeuralFile.GetW2(h, o);
          }
       }
     } else {
        randomizeWeights();
     }

     output_errors = new double[NumOutputs];
     hidden_errors = new double[NumHidden];

     // Get the training cases (if any) from the training file:
     LoadTrainingCases();

   }

   public void LoadTrainingCases() {
     NumTraining = NeuralFile.NumTraining;
     if (NumTraining > 0) {
        InputTraining  = new double[NumTraining * NumInputs];
        OutputTraining = new double[NumTraining * NumOutputs];
     }
     int ic=0, oc=0;

     for (int k=0; k<NumTraining; k++) {
        for (int i=0; i<NumInputs; i++)
            InputTraining[ic++] = NeuralFile.GetInput(k, i);
        for (int o=0; o<NumOutputs; o++)
            OutputTraining[oc++] = NeuralFile.GetOutput(k, o);
     }
   }

   Neural(int i, int h, int o) {
     System.out.println("In BackProp constructor");
     Inputs = new double[i];
     Hidden = new double[h];
     Outputs = new double[o];
     W1 = new double[i][h];
     W2 = new double[h][o];
     NumInputs = i;
     NumHidden = h;
     NumOutputs = o;
     output_errors = new double[NumOutputs];
     hidden_errors = new double[NumHidden];

     // Randomize weights here:
     randomizeWeights();
   }

   void Save(String output_file) {
     if (NeuralFile==null) {
        System.out.println("Error: no NeuralFile object in Neual::Save");
     } else {
        for (int i=0; i<NumInputs; i++) {
          for (int h=0; h<NumHidden; h++) {
            NeuralFile.SetW1(i, h, W1[i][h]);
          }
        }
        for (int h=0; h<NumHidden; h++) {
          for (int o=0; o<NumOutputs; o++) {
            NeuralFile.SetW2(h, o, W2[h][o]);
          }
        }
        NeuralFile.Save(output_file);
     }
   }

   public void randomizeWeights() {
    // Randomize weights here:
     for (int ii=0; ii<NumInputs; ii++)
        for (int hh=0; hh<NumHidden; hh++)
           W1[ii][hh] =
              0.1f * (double)Math.random() - 0.05f;
     for (int hh=0; hh<NumHidden; hh++)
        for (int oo=0; oo<NumOutputs; oo++)
           W2[hh][oo] =
              0.1f * (double)Math.random() - 0.05f;
   }

   public void ForwardPass() {
       int i, h, o;
       for (h=0; h<NumHidden; h++) {
         Hidden[h] = 0.0f;
       }
       for (i=0; i<NumInputs; i++) {
           for (h=0; h<NumHidden; h++) {
                Hidden[h] += Inputs[i] * W1[i][h];
           }
       }
       for (o=0; o<NumOutputs; o++)
         Outputs[o] = 0.0f;
       for (h=0; h<NumHidden; h++) {
           for (o=0; o<NumOutputs; o++) {
                Outputs[o] += Sigmoid(Hidden[h]) * W2[h][o];
           }
       }
       for (o=0; o<NumOutputs; o++)
         Outputs[o] = Sigmoid(Outputs[o]);
  }

  public double Train() {
     return Train(InputTraining, OutputTraining, NumTraining);
  }

  public double Train(double ins[],
                     double outs[],
                     int num_cases) {
    int i, h, o;
    int in_count=0, out_count=0;
    double error = 0.0f;
    for (int example=0; example<num_cases; example++) {
      if (IgnoreTraining != null)
         if (IgnoreTraining[example]) continue;  // skip this case
      // zero out error arrays:
      for (h=0; h<NumHidden; h++)
         hidden_errors[h] = 0.0f;
      for (o=0; o<NumOutputs; o++)
         output_errors[o] = 0.0f;
      // copy the input values:
      for (i=0; i<NumInputs; i++) {
         Inputs[i] = ins[in_count++];
      }

      if (IgnoreInput != null) {
        for (int ii=0; ii<NumInputs; ii++) {
           if (IgnoreInput[ii]) {
             for (int hh=0; hh<NumHidden; hh++) {
                W1[ii][hh] = 0;
             }
           }
        }
      }

      // perform a forward pass through the network:

      ForwardPass();

      if (MyGUI != null)  MyGUI.repaint();
      for (o=0; o<NumOutputs; o++)  {
          output_errors[o] = (outs[out_count++] - Outputs[o]) * SigmoidP(Outputs[o]);
      }
      for (h=0; h<NumHidden; h++) {
        hidden_errors[h] = 0.0f;
        for (o=0; o<NumOutputs; o++) {
           hidden_errors[h] += output_errors[o]*W2[h][o];
        }
      }
      for (h=0; h<NumHidden; h++) {
         hidden_errors[h] = hidden_errors[h] * SigmoidP(Hidden[h]);
      }
      // update the hidden to output weights:
      for (o=0; o<NumOutputs; o++) {
         for (h=0; h<NumHidden; h++) {
            W2[h][o] +=
               0.5 * output_errors[o] * Hidden[h];
         }
      }
      // update the input to hidden weights:
      for (h=0; h<NumHidden; h++) {
         for (i=0; i<NumInputs; i++) {
             W1[i][h] +=
                0.5 * hidden_errors[h] * Inputs[i];
         }
      }
      for (o=0; o<NumOutputs; o++)
          error += Math.abs(output_errors[o]);
    }
    return error;
  }

  protected double Sigmoid(double x) {
    return
     (double)((1.0f/(1.0f+Math.exp((double)(-x))))-0.5f);
  }

  protected double SigmoidP(double x) {
    double z = Sigmoid(x) + 0.5f;
    return (double)(z * (1.0f - z));
  }

}
