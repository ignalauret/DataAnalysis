function [] = myPlot(M, tag1, tag2, tag3, ml, f, t);

  #{
      Plots a 3-Row data matrix.

      Row 1 = Blue, Subplot 1, tag1.
      Row 2 = Green, Subplot 2, tag2.
      Row 3 = Red, Subplot 3, tag3.

      ! Parameters:
        M = 3-Row data Matrix.
        tag1 = first Row tag.
        tag2 = second Row tag.
        tag3 = third Row tag.
        ml = Mask Level
        f = Window number (For multiple plots).
        t = Titulo
  #}

# Check if M is a 3-row matrix.
  if (size(M,1) != 3)
    disp("Wrong matrix size");
    disp("Expected rows: 3");
    disp(["The matrix you want to plot has: ", size(M,1)]);
    return;
  end

# Applies a mask to the data.
  maskData = mask(M, ml);

# Get the masked Data size.
  newSize = size(maskData, 2);

# Create linspace for the plot.
  data = [ linspace(1, newSize, newSize) ; maskData];

# Create new window for the plot.
  figure(f);

# Plot the data.
  # Row 1
    subplot(3,1,1)
    plot(data(1,:),data(2,:));
    title(t);
    legend(tag1);

    # Row 2
    subplot(3,1,2)
    plot(data(1,:),data(3,:),'g');
    legend(tag2);

    # Row 3
    subplot(3,1,3)
    plot(data(1,:),data(4,:),'r');
    legend(tag3);

end
