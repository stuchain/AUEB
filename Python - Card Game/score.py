
#from medium import card1
#from hard import card1
def score(card1,card2,way):
    sc=0
    if way == 1: 
        if card1[0]=='1' or card1[0]=='K' or card1[0]=='J' or card1[0]=='Q':
            sc=10
        elif card1[0]=='A':
            sc=1    
        else:
            sc=int(card1[0]) 

    else:
        if card1[0]==card2[0]:
            if card1[0]=='1' or card1[0]=='K' or card1[0]=='J' or card1[0]=='Q':
                sc=10
            elif card1[0]=='A':
                sc=1    
            else:
                sc=int(card1[0])
        elif card1[0]=='1' and card2[0]=='1':
            if card1[2]==card2[2]:
                sc+=20
        elif card1[0]=='1':
            if card1[2]==card2[1]:
                if card2[0]=='K' or card2[0]=='J' or card2[0]=='Q':
                    sc+=20
                elif card2[0]=='A':
                    sc+=11   
                else:
                    sc+=int(card2[0])+10
        elif card2[0]=='1':
            if card2[2]==card1[1]:
                if card1[0]=='K' or card1[0]=='J' or card1[0]=='Q':
                    sc+=20
                elif card1[0]=='A':
                    sc+=11   
                else:
                    sc+=int(card1[0])+10
        else:
            if card1[1]==card2[1]:
                if card1[0]=='K' or card1[0]=='J' or card1[0]=='Q':
                    sc+=10
                elif card1[0]=='A':
                    sc+=1   
                else:
                    sc+=int(card1[0])
                if card2[0]=='K' or card2[0]=='J' or card2[0]=='Q':
                    sc+=10
                elif card2[0]=='A':
                    sc+=1   
                else:
                    sc+=int(card2[0])
                


    return sc     

