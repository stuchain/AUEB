# CN - P2P File Sharing Implementation

Implementation module for the P2P file-sharing system, containing the tracker and peer client source code.

Parent project overview: [`../README.md`](../README.md)

## Architecture

The module follows a tracker-coordinated model:

- `Tracker` accepts peer connections and maintains user/file metadata.
- `Peer` provides an interactive CLI for register/login/inform/list/details/check/notify/logout actions.
- `PeerInfo` stores runtime user and connection state.
- `PhotoConfirmationDialog` provides a small Swing confirmation dialog in specific download flows.

Core source files:
- [`src/Tracker.java`](src/Tracker.java)
- [`src/Peer.java`](src/Peer.java)
- [`src/PeerInfo.java`](src/PeerInfo.java)
- [`src/PhotoConfirmationDialog.java`](src/PhotoConfirmationDialog.java)

## Protocol Responsibilities

- `Register`/`Login`: create peer identity and active session.
- `Inform`: publish available files to tracker metadata.
- `List`/`Details`: discover peers and shared file information.
- `checkActive`: validate candidate peers before download.
- `notify`: update transfer status and counters.
- `Logout`/`Exit`: close active session cleanly.

## Build and Run

Prerequisite: [JDK](https://www.oracle.com/java/technologies/downloads/)

From this folder (`CN`):

1. Compile:
   - `javac -d out src/*.java`
2. Start tracker (Terminal 1):
   - `java -cp out Tracker`
3. Start one or more peers (Terminal 2+):
   - `java -cp out Peer`

## Workflow

Typical session:

1. Start `Tracker`.
2. Start peer clients.
3. Register and login users.
4. Inform tracker about available files.
5. Use list/details/check operations to discover files and active peers.
6. Trigger download workflow and send status notifications.
7. Logout and exit peers.

## Project Structure

- `src/` - Java source files.
- `fileDownloadList.txt` - local download tracking list used by workflow.
- Runtime shared files may be generated under `Shared_directory/`.

## Troubleshooting

- If peers cannot connect, verify tracker is already running.
- If runtime files are missing, launch from the `CN` directory so relative paths resolve correctly.
- If port conflicts occur, stop old processes or adjust fixed ports in code.

## Known Limitations

- Uses fixed/default ports unless code is changed.
- File and network paths are partially runtime-assumption based.
- Current implementation emphasizes discovery/coordination workflow; end-to-end binary transfer behavior is limited.

## References

- [Java Sockets Tutorial](https://docs.oracle.com/javase/tutorial/networking/sockets/)
- [Java Concurrency Tutorial](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
