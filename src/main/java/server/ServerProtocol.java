package server;

import controller.AuctionController;
import controller.BidController;
import controller.ItemController;
import controller.UserController;
import model.Auction;
import model.Bid;
import model.Item;
import model.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;

import static server.CristoServer.*;

public class ServerProtocol {
    private static final String HEADER = "PROTOCOLCRISTOBAY1.0";
    private static final int PACKAGE_SIZE = 256;
    private final SocketThread socket;
    private String[] args;
    private String imgB64 = null;
    private static final String ABSOLUTE_PATH = "/Users/miguel/source/recu/Cristo-Bay/CristoServer/";

    public ServerProtocol(SocketThread socket) {
        this.socket = socket;
    }

    public void processInput (String input) {
        socket.signedPrint(input);
        args = input.replace("\"","").replace("'","").split("#");
        if (args.length < 1 || !args[0].equals(HEADER)) {
            ui.consolePrint("invalid message: " + input);
            return;
        }
        String[] request = args[1].split("_");
        String formattedRequest = args[1];
        switch (request[0]) {
            case "REFRESH" -> formattedRequest = "REFRESH";
            case "GET" -> {
                if (request[1].equals("SUBASTAS")) {
                    formattedRequest = "GET_SUBASTAS";
                }
            }
        }
        switch (formattedRequest) {
            case "LOGIN" -> checkLogin();
            case "BID_PRODUCT" -> checkBid();
            case "GET_PROFILE" -> sendProfile();
            case "GET_CONNECTED_USERS" -> sendConnectedUsers();
            case "REFRESH" -> sendRefresh(Auction.Status.valueOf(request[1]));
            case "GET_SUBASTAS" -> sendAuctions(Auction.Status.valueOf(request[2]));
            case "GET_SUBASTA" -> sendAuction();
            case "PREPARED_TO_RECEIVE" -> sendImage();
            case "BYE" -> logout();
            default -> socket.signedPrint("ERROR: unknown command: " + formattedRequest);
        }
    }

    private boolean validateSession(String username, String token) {
        return username.equals(socket.user.getUsername()) && token.equals(socket.getName());
    }

    private void login(User user) {
        UserController.connect(user);
        user.setConnected(true);
        socket.user = user;
        ui.updateClientsTable();
        socket.sendAndPrintData(HEADER +
                "#WELLCOME#" + socket.user.getUsername() +
                "#WITH_TOKEN#" + socket.getName());
    }

    private void checkLogin() {
        User user = UserController.getUserByCredentials(args[2], args[3]);
        if (user !=null) {
            if (!user.isConnected()) {
                login(user);
            } else {
                for (SocketThread st : sockets) {
                    if (st.user != null && st.user.getUsername().equals(user.getUsername())) {
                        socket.signedPrint("user " + st.user.getUsername() + " is already connected");
                        socket.sendAndPrintData(HEADER + "#ERROR#BAD_LOGIN");
                        return;
                    }
                    login(user);
                }
            }
        } else {
            socket.signedPrint("wrong credentials");
            socket.sendAndPrintData(HEADER + "#ERROR#BAD_LOGIN");
        }
    }

    private void checkBid() {
        if (validateSession(args[2], args[3])) {
            String[] auctionData = args[4].split("@");
            User user = UserController.getUserByUsername(args[2]);
            Auction auction = AuctionController.getAuctionByIdDate(Integer.parseInt(auctionData[0]), Timestamp.valueOf(auctionData[1]), Timestamp.valueOf(auctionData[2]));
            if (auction != null) {
                int max_bid = BidController.getMaxBid(auction.getItemId(), auction.getStartDate(), auction.getEndDate());
                if (auction.getUserId() != user.getId() && Integer.parseInt(args[5]) > max_bid && auction.getStatus() == Auction.Status.OPEN) {
                    Bid bid = new Bid();
                    bid.setDate(new Timestamp(System.currentTimeMillis()));
                    bid.setUserId(user.getId());
                    bid.setItemId(auction.getItemId());
                    bid.setStartDate(auction.getStartDate());
                    bid.setEndDate(auction.getEndDate());
                    bid.setBidAmount(Double.parseDouble(args[5]));
                    BidController.setBid(bid);
                    for (SocketThread st : sockets) {
                        st.sendAndPrintData(HEADER + "#BID_ACCEPTED" +
                                "#" + bid.getItemId() +
                                "@" + bid.getStartDate() +
                                "@" + bid.getEndDate() +
                                "#" + user.getUsername() +
                                "#" + bid.getStartDate() +
                                "@" + bid.getBidAmount());
                    }
                } else {
                    socket.signedPrint("");
                    socket.sendAndPrintData(HEADER + "#BID_REJECTED" +
                                    "#" + auction.getItemId() +
                                    "@" + auction.getStartDate() +
                                    "@" + auction.getEndDate() +
                                    "#" + user.getUsername());
                }
            } else {
                socket.signedPrint("ERROR: auction does not exists");
            }
        }
    }

    private void sendProfile() {
        if (validateSession(args[2], args[3])) {
            User user = UserController.getUserByUsername(args[2]);
            ArrayList<Bid> bids = BidController.getBidsByBidder(user.getId());
            if (bids != null) {
                ArrayList<Auction> auctions= new ArrayList<>();
                for (Bid bid : bids) {
                    Auction auction = AuctionController.getAuctionByIdDate(bid.getItemId(), bid.getStartDate(), bid.getEndDate());
                    if (!auctions.contains(auction)) {
                        auctions.add(auction);
                    }
                }
                StringBuilder data = new StringBuilder(HEADER + "#AUCTIONS_PROFILE#" + auctions.size());
                for (Auction auction : auctions) {
                    Item item = ItemController.getItemById(auction.getItemId());
                    User vendor = UserController.getUserById(auction.getUserId());
                    int max_bid = BidController.getMaxBid(auction.getItemId(), auction.getStartDate(), auction.getEndDate());
                    data.append("#").append(auction.getItemId())
                            .append("@").append(auction.getStartDate())
                            .append("@").append(auction.getEndDate())
                            .append("@").append(auction.getStatus())
                            .append("@").append(item.getName())
                            .append("@").append(vendor.getName())
                            .append("@").append(auction.getStartingPrice())
                            .append("@").append(max_bid);
                }
                socket.sendAndPrintData(data.toString());
            } else {
                socket.sendAndPrintData(HEADER + "#ERROR#CANT_GET_PROFILE");
            }
        } else {
            socket.signedPrint("ERROR: not logged in");
        }
    }

    private void sendConnectedUsers() {
        if (validateSession(args[2], args[3])) {
            ArrayList<User> users = UserController.getConnectedUsers();
            if (users != null) {
                StringBuilder data = new StringBuilder(HEADER + "#CONNECTED_USERS#" + users.size());
                for (User user : users) {
                    data.append("#").append(user.getName());
                }
                socket.sendAndPrintData(data.toString());
            } else {
                socket.sendAndPrintData(HEADER + "#ERROR#CANT_GET_USERS");
            }
        } else {
            socket.signedPrint("ERROR: not logged in");
        }
    }

    private void sendRefresh(Auction.Status status) {
        ArrayList<Auction> auctions = AuctionController.getAuctionsByStatus(status);
        if (auctions != null) {
            StringBuilder data = new StringBuilder(HEADER + "#REFRESH_AUCTIONS#" + auctions.size());
            for (Auction auction : auctions) {
                Item item = ItemController.getItemById(auction.getItemId());
                User vendor = UserController.getUserById(auction.getUserId());
                int max_bid = BidController.getMaxBid(auction.getItemId(), auction.getStartDate(), auction.getEndDate());
                data.append("#").append(item.getId())
                        .append("@").append(auction.getStartDate())
                        .append("@").append(auction.getEndDate())
                        .append("@").append(AuctionController.statusToEstado(auction.getStatus()))
                        .append("@").append(item.getName())
                        .append("@").append(vendor.getName())
                        .append("@").append(auction.getStartingPrice())
                        .append("@").append(max_bid);
            }
            socket.signedPrint("enviando refresh a " + socket.getName());
            socket.sendAndPrintData(data.toString());
        } else {
            socket.sendAndPrintData(HEADER + "#ERROR#AUCTION_NOT_AVAILABLE");
        }

    }

    private void sendAuctions(Auction.Status status) {
        if (validateSession(args[2], args[3])) {
            ArrayList<Auction> auctions = AuctionController.getAuctionsByStatus(status);
            if (auctions != null) {
                StringBuilder data = new StringBuilder(HEADER + "#AUCTION_AVAILABLE#" + auctions.size());
                for (Auction auction : auctions) {
                    Item item = ItemController.getItemById(auction.getItemId());
                    User vendor = UserController.getUserById(auction.getUserId());
                    int max_bid = BidController.getMaxBid(auction.getItemId(), auction.getStartDate(), auction.getEndDate());
                    data.append("#").append(item.getId())
                            .append("@").append(auction.getStartDate())
                            .append("@").append(auction.getEndDate())
                            .append("@").append(AuctionController.statusToEstado(auction.getStatus()))
                            .append("@").append(item.getName())
                            .append("@").append(vendor.getName())
                            .append("@").append(auction.getStartingPrice())
                            .append("@").append(max_bid);
                }
                socket.sendAndPrintData(data.toString());
            } else {
                socket.sendAndPrintData(HEADER + "#ERROR#AUCTION_NOT_AVAILABLE");
            }
        } else {
            socket.signedPrint("ERROR: not logged in");
        }
    }

    private void setImgB64(String path) {
        if (path == null) {
            imgB64 = null;
        } else {
            try {
                byte[] fileContent = Files.readAllBytes(new File(ABSOLUTE_PATH + path).toPath());
                imgB64 = Base64.getEncoder().encodeToString(fileContent);
            } catch (IOException e) {
                ui.consolePrint("ERROR setImgB64");
                throw new RuntimeException(e);
            }
        }
    }

        private void sendAuction() {
            if (validateSession(args[2], args[3])) {
            String[] auctionData = args[4].split("@");
            Auction auction = AuctionController.getAuctionByIdDate(Integer.parseInt(auctionData[0]), Timestamp.valueOf(auctionData[1]), Timestamp.valueOf(auctionData[2]));
            if (auction != null) {
                Item item = ItemController.getItemById(auction.getItemId());
                setImgB64(item.getPicture());
                socket.sendAndPrintData(HEADER + "#GET_SUBASTA"
                        .concat("#" + auction.getItemId())
                        .concat("@" + auction.getStartDate())
                        .concat("@" + auction.getEndDate())
                        .concat("#" + item.getDescription())
                        .concat("#" + ((item.getPicture() != null) ? item.getPicture().substring(item.getPicture().lastIndexOf('.')+1) : null))
                        .concat("#" + (imgB64 != null ? String.valueOf(imgB64.length()) : 0)));
            } else {
                CristoServer.ui.consolePrint("ERROR: auction does not exists");
            }
        } else {
            CristoServer.ui.consolePrint("ERROR: not logged in");
        }
    }

    private void sendImage() {
        if (validateSession(args[2], args[3])) {
            int item_id = Integer.parseInt(args[4]);
            if (imgB64 != null) {
                socket.signedPrint("sending Image...");
                int n_package = 0;
                //todo: the right way
//                int package_size = Integer.parseInt(args[6]);
//                for (int i = 0; i < imgB64.length(); i+=package_size) {
                for (int i = 0; i < imgB64.length(); i+=PACKAGE_SIZE) {
                    socket.sendData(HEADER
                            .concat("#" + item_id)
                            //todo: the right way
                            //.concat("#" + imgB64.substring(i, Math.min(i + package_size, imgB64.length())))
                            .concat("#" + imgB64.substring(i, Math.min(i + PACKAGE_SIZE, imgB64.length())))
                            //todo BITS
                            //.concat("#" + n_package));
                            .concat("#BITS" + n_package));
                    n_package++;
                }
                socket.signedPrint("image sent. n_packages: " + (n_package-1));
            } else {
                socket.signedPrint("this article does not have image...");
            }
        } else {
            CristoServer.ui.consolePrint("ERROR: not logged in");
        }
    }

    private void logout() {
        if (socket.user != null) {
            UserController.disconnect(socket.user);
            socket.sendAndPrintData(HEADER + "#VAYAUSTEDCONDIOS#" + socket.user.getName() + "#" + socket.getName());
        } else {
            socket.sendAndPrintData(HEADER + "#VAYAUSTEDCONDIOS");
        }
        try {
            socket.socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        socket.signedPrint("disconnected");
        sockets.remove(socket);
        ui.updateClientsTable();
    }

    public static void serverShutdown() {
        for (SocketThread socket : sockets) {
            if (socket.user != null) {
                UserController.disconnect(socket.user);
                socket.sendAndPrintData(HEADER + "#VAYAUSTEDCONDIOS#" + socket.user.getName() + "#" + socket.getName());
            } else {
                socket.sendAndPrintData(HEADER + "#VAYAUSTEDCONDIOS");
            }
        }
    }
}
