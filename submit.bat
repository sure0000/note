
set currentTime = %date% %time%
git add .
git commit -m '%currentTime%'
git push origin master
