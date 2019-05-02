function [J] = mask(X, ml)

#{
    Applies a mask to a data matrix to reduce the noise on it.

    ! Parameters:
      ml = mask level
        ! [ more ml => less noise => less data ]
#}

# Get the size of M.
  n = size(X, 1);
  m = size(X, 2);

# Return matrix J.
  J = zeros(n, m-(2*i)-1);

# Apply mask on the data and saves it in J.
  for g = 1:n
    for j = ml+1 : (m-(ml+1))
      for h = 1 : ml
        J(g, j-ml) = J(g, j-ml) + X(g, j-h) + X(g, j+h);
      end
      J(g, j-ml) = (J(g, j-ml) + X(g, j)) / (2*ml+1);
    end
  end

end
