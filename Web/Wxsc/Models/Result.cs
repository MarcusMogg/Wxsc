using System;

namespace Wxsc.Models
{
    public class Result
    {
        public Result(int status, string info)
        {
            Status = status;
            Info = info;
        }

        public int Status { get; set; }
        public string Info { get; set; }
    }
}
