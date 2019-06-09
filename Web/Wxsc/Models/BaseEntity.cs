using System.ComponentModel.DataAnnotations;

namespace Wxsc.Models
{
    public class BaseEntity
    {
        [Key] public int Id { get; set; }
    }
}
