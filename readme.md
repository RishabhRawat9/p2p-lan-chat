# fix ui - > maybe use lanterna

# handle execeptions properly -> when any of the sockets close exceptions gets thrown that shouldn't happen and the whole app shouldn't crash
# drop out peers that get disconnected by checking their vitals every few seconds and removing them from the list;
# handle multiple peers and 
# make the ui look more cool.
# i think to handle multiple clients i will have to just keep one main guy which i currently send the message to and then like keep a list or in the peerInfo i store their in, out streams for the tcp socket and which ever peer i select i can talk to them but i have to keep the messages stored somewhere; maybe use the h2 database?
# to remove the offline peers from the list we can maybe keep a ttl like a thing in the peerInfo and check the ttl after some time and then remove the peers who's ttl expired?


# abhi to me same device pe test krrha hu esliye ports change krne pd rhe hai but later they'll be running on the same ports across devices so that everything works;

# work on integrating h2 and store messages as long as the app is running and later add multi peer support;
# --> ok the thing is we want to allow multiple peers connected in one go and for one session we wanna have all the messages stored as long as the session runs so we want to store peers , along with them we want to store their messages and when we switch b/w peers we load their chat and see the history of the messages for the current session.

