function [A,D,R,AC] = analyzeData(s);

  #{
      1. Uploads the four matrices:
        Axis Matrix (A) from "/AxisMatrix" ++ $s ++ .csv
          ! The axis that the app calculated
        Raw Driving Data Matrix (R) from "/RawDrivingData" ++ $s ++ .csv
          ! The Raw data collected by de app
        Corrected Driving Data Matrix (D) from "/DrivingData" ++ $s ++ .csv
          ! The Raw data corrected with the calculated axis
        Axis Calculation Data Matrix (AC) from "/AxisCalculationData" ++ $s ++ .csv
          ! The data used by the app to calculate the axis

      2. Prints the three Axis on terminal.
      3. Plots the Raw data and the Corrected data for comparision.
      4. Plots the Axis Calculation Data for Analysis.

      ! Parameters:
        s = "_hh_mm_ss_" tag from the data collection round we want to analyze.

      ! Returns the 4 matrices if needed for further analysis.
  #}

  # Define the level of mask that will be applied on the data for plotting.
  MASK_LEVEL = 10;

  # Upload the four files from $s data collection round.
  A = load(["AxisMatrix" s ".csv"]);
  D = load(["DrivingData" s ".csv"]);
  R = load(["RawDrivingData" s ".csv"]);
  AC = load(["AxisCalculationData" s ".csv"]);

  # Print Axis on terminal.
  X_Axis = A(1,:)
  Y_Axis = A(2,:)
  Z_Axis = A(3,:)

  # Plots the Axis Calculation Data.
  myPlot(AC, "X Axis", "Y Axis", "Z Axis", MASK_LEVEL, 3, "Axis Calculation Data")

  # Plots the Raw data and the Corrected data.
  compareData( R, "Raw Data", D, "Corrected Data", MASK_LEVEL);

end
