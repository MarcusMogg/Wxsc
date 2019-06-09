using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using TensorFlow;

namespace Wxsc.TF
{
    public class TfData
    {
        public TFGraph Graph { get; set; }

        public List<string> Labels { get; } 

        private static TfData _tfData;

        private TfData()
        {
            Graph = new TFGraph();
            var model = File.ReadAllBytes("frozen_model.pb");
            Graph.Import(model);
            
            
            Labels = File.ReadAllText("labels.txt").Split(",").ToList();
        }

        public static TfData GeTfData()
        {
            if (_tfData == null)
            {
                _tfData = new TfData();
            }

            return _tfData;
        }

    }
}
