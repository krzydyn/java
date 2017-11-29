package tele;

//ftp://ftp.dcs.shef.ac.uk/share/nlp/amities/resources/forAlbany/sr5.1/TMPP/doc/htmlfiles/gcss7tug/1380-03t.htm

//MAP-MT-FORWARD-SHORT-MESSAGE-REQ (TLV!!!)
//4517090007320807001010151809040797440000000019199e400a915413131215000070206091553200a0050003e10201363c180c0693ddc2b78d0f447dbbf320213b9c9683d0e139681e4e9341e8f41c647ecbcbe9b31b047fb3d3e33c283d0789c66f375dfeb697e5f374982d0289eb74103a3d0785e170f93b3c4683de66503bcd4ed3c3f23c28eda697e5f6b29b9e7ebb41edfa9c0e1abfddf4b4bb5e7681926e5018d40eabdf72d01c5e2e8fd12c10ba0c9a87d31a000e010100

// Signaling System 7
public class SS7 {
	static class Physical {
		enum LinkType {
			A, B, C, D, E, F
		}

		//Each node on the network is identified b a number
		enum SignalNode {
			SSP,  //Service Switching Points
			STP,  //Signal Transfer Points
			SCP   //Service Control Points
		}

		//The links between nodes are full-duplex 56, 64, 1536, or 1984 kbit/s
		//In Europe, SS7 links normally are directly connected between switching exchanges using F-links.
		//   This direct connection is called associated signaling.
		//In North America, SS7 links are normally indirectly connected between switching exchanges using an intervening network of STPs.
		//   This indirect connection is called quasi-associated signaling

		//SS7 links at higher signaling capacity (1.536 and 1.984 Mbit/s, simply referred to as the 1.5 Mbit/s and 2.0 Mbit/s rates)
		// are called high speed links (HSL)
	}

	//  SS7 protocol, referred to as the Network Service Part (NSP)
	static class Protocol {
		int l2_crc16;
		enum OSILayer {
			L1_MTP,  //Physical, Message Transfer Part
			L2_MTP,  //DataLink (message packets)
			//Network Service Part
			L3_MTP,  //Network
			L4_SCCP, //Signaling Connection Control Part

			//Application layer
			L7_INAP,
			L7_MAP,
		}
	}

	static class BICC {
		int cic; //4 bytes = call instance code
		byte mt; //1 byte = message type
		byte[] packet; //????
	}
	static class BISUP {
		byte mt;   //message type
		short len; //message length
		byte cinfo;//message compat info
		byte[] msg;//message content
	}
	static class DUP {

	}
	static class ISUP {

	}

	private Physical.LinkType link;

}

