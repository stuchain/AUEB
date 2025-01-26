from easy import easy
from easy1 import easy1
from medium import medium
from medium1 import medium1
from hard import hard
from hard1 import hard1
print('Καλωσήλθατε στο Matching Game')
players=int(input('Δώστε αριθμό παιχτών: '))
while players<2:
    print('Ο αριθμός παικτών πρέπει να είναι πάνω από 2')
    players=int(input('Δώστε αριθμό παιχτών: '))
#arxikopoiw th lista gia to score twn paixtwn
score=[0]*players    
dif=int(input('Δώστε επίπεδο δυσκολίας Εύκολο(1), Μέτριο(2), Δύσκολο(3): '))  
while dif!=1 and dif!=2 and dif!=3:
    print('Δώστε έγκυρο επίπεδο δυσκολίας')
    dif=int(input('Δώστε επίπεδο δυσκολίας Εύκολο(1), Μέτριο(2), Δύσκολο(3): '))
way=int(input('Επίλεξε τρόπο διεξαγωγής παιχνιδιού Classic(1), Figures(2): '))
while way!=1 and way!=2:
    print('Δώστε έγκυρο τρόπο διεξαγωγής')
    way=int(input('Επίλεξε τρόπο διεξαγωγής παιχνιδιού Classic(1), Figures(2): '))
if dif == 1:
    if way==1:
        easy(players,way)
    else:
        easy1(players,way)
elif dif == 2:
    if way == 1:
        medium(players,way)
    else:
        medium1(players,way)
elif dif == 3:
    if way == 1:
        hard(players,way)
    else:
        hard1(players,way)

import time
time.sleep(10)    # Pause 10 seconds

