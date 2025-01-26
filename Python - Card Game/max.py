def maxer(score_list,players):
    maxi=score_list[0]
    j=0
    for i in range (1,players):
        if score_list[i]>maxi:
            maxi=score_list[i]
            j=i
    k=0       
    for i in range (players):
        if maxi== score_list[i]:
            k+=1
    if k>1:
        score_names=[0]*players
        for i in range (players):
            if score_list[i]==maxi:
                print('Ο Παίκτης, ',i+1,'ειναι ένας απο τους ισσόπαλους παίκτες με σκόρ',maxi)
    else:
        print('Νικητής ειναι ο Παίκτης ',j+1,' με σκόρ:',maxi)

