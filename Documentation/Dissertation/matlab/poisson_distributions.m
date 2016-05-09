mu5poisson = zeros(51)
mu5poisson = mu5poisson(1,:)
mu128poisson = zeros(51)
mu128poisson = mu128poisson(1,:)
mu245poisson = zeros(51)
mu245poisson = mu245poisson(1,:)
pmu5 = exp(-5)
pmu128 = exp(-15)
pmu245 = exp(-25)

for i = 1:51
    mu5poisson(i) = pmu5
    pmu5 = pmu5 * 5 / i;
    mu128poisson(i) = pmu128
    pmu128 = pmu128 * 15 / i;     
    mu245poisson(i) = pmu245
    pmu245 = pmu245 * 25 / i; 
end

subplot(2,3,1)
bar(mu5poisson, 'EdgeColor','blue')
title('Poisson(5)')


subplot(2,3,2)
bar(mu128poisson, 'EdgeColor','blue')
title('Poisson(15)')


subplot(2,3,3)
bar(mu245poisson, 'EdgeColor','blue')
title('Poisson(25)')


mu5spoisson = mu5poisson
mu128spoisson = mu128poisson
mu245spoisson = mu245poisson

for i = 30:51 
   mu5spoisson(i) = 0
   mu128spoisson(i) = 0
   mu245spoisson(i) = 0
end


mu5spoisson(30) = 1
mu128spoisson(30) = 1
mu245spoisson(30) = 1

for i = 1:29 
    mu5spoisson(30) = mu5spoisson(30) - mu5spoisson(i)
    mu128spoisson(30) = mu128spoisson(30) - mu128spoisson(i)
    mu245spoisson(30) = mu245spoisson(30) - mu245spoisson(i)
end


subplot(2,3,4)
bar(mu5spoisson, 'EdgeColor','blue')
title('SPoisson(5,30)')


subplot(2,3,5)
bar(mu128spoisson, 'EdgeColor','blue')
title('SPoisson(15,30)')


subplot(2,3,6)
bar(mu245spoisson, 'EdgeColor','blue')
title('SPoisson(25,30)')