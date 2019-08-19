

Description of blocks.json format:
{ // accolades around entire file (as the JSON standard dictates)
  "manufacturer" : "/your company name, may include spaces/",
  "blocks" : {
    "/name of the block, may include spaces/" : {
      "file" : "/exact file name, relative to this file. Use .. to access parent map/",
      "size" : [/integer width x/, /integer width y/, /integer height z/], // all in units
      "mass" : /integer weight, in units of a 1x1x1 block/,
      "topPoints" : [ // coordinates where buds are. Note: this is one higher than where these buds are placed on
          [/integer x/, /integer y/, /integer z/] // multiple can be added, separated with commas
        ],
      "bottomPoints" : [ // coordinates where buds can be attached
          [/integer x/, /integer y/, /integer z/]
        ]
    } // multiple blocks can be added, separated with commas
  }
}
