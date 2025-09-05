# fix ui - > maybe use lanterna

# handle execeptions properly -> when any of the sockets close exceptions gets thrown that shouldn't happen and the whole app shouldn't crash
# drop out peers that get disconnected by checking their vitals every few seconds and removing them from the list;
# the error with trying to chat again to the user is unable to setup the socket with the same local port and ip it's because java takes around 1-3 mins before you can reuse the same socket again .--> don't have to bind explicitly just use os given ports;
# having problems when trying to setup chat again with some peer after the first time;-> fixed
# for now stopping the project, might continue later with lanterna, handling multiple peers rather than just one.

hahhaha