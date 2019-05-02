function [] = compareData(M1, tag1, M2, tag2, ml);

#{
    Plots two data matrices with their respective tags for comparission.

    ! Parameters:
      M1,M2 = The matrices with the data for comparing.
      tag1,tag2 = The tags for plotting.
      ml = Mas level for plotting.
#}

# Plots M1 with maskLevel = ml, Figure = 1, tag = tag1.
  myPlot(M1, "X Axis", "Y Axis", "Z Axis", ml, 1, tag1);

# Plots M2 with maskLevel = ml, Figure = 2, tag = tag2.
  myPlot(M2, "X Axis", "Y Axis", "Z Axis", ml, 2, tag2);
  
end
