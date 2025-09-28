# fix ui - > maybe use lanterna

# handle execeptions properly -> when any of the sockets close exceptions gets thrown that shouldn't happen and the whole app shouldn't crash
# drop out peers that get disconnected by checking their vitals every few seconds and removing them from the list;
# handle multiple peers and 
# make the ui look more cool.
# i think to handle multiple clients i will have to just keep one main guy which i currently send the message to and then like keep a list or in the peerInfo i store their in, out streams for the tcp socket and which ever peer i select i can talk to them but i have to keep the messages stored somewhere; maybe use the h2 database?
# to remove the offline peers from the list we can maybe keep a ttl like a thing in the peerInfo and check the ttl after some time and then remove the peers who's ttl expired?


# abhi to me same device pe test krrha hu esliye ports change krne pd rhe hai but later they'll be running on the same ports across devices so that everything works;


# fix the peer detection system, right now when one peer is detected by the udp server we store that peer in the list and that peer just stays in the list even though he has gone offline , we need to fix that .
 # --> so thinking of adding a ttl field with each peer, and a background thread running which invalidates each peer after their ttl hits 0; when the peer's udp packet is received again we reset their ttl;

# ok so now using the custom hashcode in PeerInfo to update or discard the dup peers;
# now the next thign is we need to periodically invalidate/validate the values in the map;
# so everytime a new packet arrives then only i check the whole map what values are valid and what are not , but how do i reduce the ttl?
# now here there is a tradeoff do i run a separate thread which manages this peer invalidation logic or do i do it in the listener thread only, doing it in the listener thread is kind of fast because i do stuff only when it's required and not periodically which can lead to inconsistent results, doing it in a separate thread makes it consistent but ineffecient if the peers are more but that's not possible as we only have it for lan and therefore the value can't be more than 255 or 254 i think, so going with the separate thread one ; 

# dup peer isn't working because i store in the map as (id, peer) so the peer is not the key, that'w why the custom equals() and hashcode() arent' working when new entries are being added in the list
# do i even need to store in the map, like i only need the peers to be stored right, so why not just use a hashset?coz when selecting a peer i would need to select them based on their id's;

# rather than keeping straight up size as ids for the peers how about i assign a id according to the peer and keep it inside the peer aswell, so by doing that i can ensure that id's are unique, so use the peerInfo's (tcpCHatPort and addr), so now i would have a unique id for each peer and i don't even have to store 


